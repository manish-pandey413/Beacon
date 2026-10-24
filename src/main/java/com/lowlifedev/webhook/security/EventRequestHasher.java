package com.lowlifedev.webhook.security;

import com.lowlifedev.webhook.dto.EventIngestRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class EventRequestHasher {

    private final ObjectMapper objectMapper;

    public EventRequestHasher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String hash(EventIngestRequest request) {
        try {
            ObjectNode normalized = objectMapper.createObjectNode();

            normalized.put("type", request.type());

            int version = request.version() == null ? 1 : request.version();

            normalized.put("version", version);

            if (request.orderingKey() == null) {
                normalized.putNull("orderingKey");
            } else {
                normalized.put("orderingKey", request.orderingKey());
            }

            normalized.set("data", canonicalize(request.data()));

            JsonNode metadata =
                request.metadata() == null
                    ? objectMapper.createObjectNode()
                    : request.metadata();

            normalized.set("metadata", canonicalize(metadata));

            if (request.occurredAt() == null) {
                normalized.putNull("occurredAt");
            } else {
                normalized.put("occurredAt", request.occurredAt().toString());
            }

            byte[] canonicalJson = objectMapper.writeValueAsBytes(
                canonicalize(normalized)
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(digest.digest(canonicalJson));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is unavailable",
                exception
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                "Unable to fingerprint event request",
                exception
            );
        }
    }

    private JsonNode canonicalize(JsonNode node) {
        if (node == null || node.isNull()) {
            return NullNode.instance;
        }

        if (node.isObject()) {
            Map<String, JsonNode> sortedFields = new TreeMap<>();
            node.properties().forEach(entry ->
                sortedFields.put(entry.getKey(), entry.getValue())
            );

            ObjectNode sorted = objectMapper.createObjectNode();
            sortedFields.forEach((name, value) ->
                sorted.set(name, canonicalize(value))
            );
            return sorted;
        }

        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                array.add(canonicalize(item));
            }
            return array;
        }

        if (node.isNumber()) {
            return objectMapper
                .getNodeFactory()
                .numberNode(node.decimalValue().stripTrailingZeros());
        }

        return node.deepCopy();
    }
}
