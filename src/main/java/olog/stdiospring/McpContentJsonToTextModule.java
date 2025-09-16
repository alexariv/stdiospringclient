package olog.stdiospring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.spec.McpSchema;

public class McpContentJsonToTextModule extends SimpleModule {

  public McpContentJsonToTextModule() {
    super("McpContentJsonToTextModule");
    addDeserializer(McpSchema.Content.class, new ContentJsonToTextDeserializer());
  }

  static final class ContentJsonToTextDeserializer extends JsonDeserializer<McpSchema.Content> {
    @Override
    public McpSchema.Content deserialize(JsonParser p, DeserializationContext ctxt) throws java.io.IOException {
      ObjectMapper mapper = (ObjectMapper) p.getCodec();
      JsonNode node = mapper.readTree(p);
      String type = node.path("type").asText("");

      // If it's already a supported type (and not "json"), let Jackson handle it normally.
      if ("text".equals(type) == false && !"json".equals(type)) {
        return mapper.convertValue(node, McpSchema.Content.class);
      }

      // Build a replacement TEXT content node
      String textValue;
      if ("json".equals(type)) {
        JsonNode payload = node.get("json");
        textValue = (payload == null)
            ? "null"
            : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
      } else {
        // type == "text": just pass through; no change
        return mapper.convertValue(node, McpSchema.Content.class);
      }

      ObjectNode replacement = JsonNodeFactory.instance.objectNode();
      replacement.put("type", "text");
      replacement.put("text", textValue);
      if (node.has("annotations")) {
        replacement.set("annotations", node.get("annotations"));
      }

      // Convert the replacement into whatever the SDK's actual TEXT subtype is.
      Class<? extends McpSchema.Content> textSubclass = resolveTextSubtype();
      if (textSubclass != null) {
        return mapper.convertValue(replacement, textSubclass);
      }
      // Fallback: let Jackson pick based on polymorphic annotations
      return mapper.convertValue(replacement, McpSchema.Content.class);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends McpSchema.Content> resolveTextSubtype() {
      try {
        Class<McpSchema.Content> base = McpSchema.Content.class;
        Class<?>[] permitted = base.getPermittedSubclasses();
        if (permitted != null) {
          for (Class<?> c : permitted) {
            String n = c.getSimpleName().toLowerCase();
            // heuristic: any permitted class whose simple name contains "text"
            if (n.contains("text") && McpSchema.Content.class.isAssignableFrom(c)) {
              return (Class<? extends McpSchema.Content>) c;
            }
          }
        }
      } catch (Throwable ignore) {}
      return null;
    }
  }
}

