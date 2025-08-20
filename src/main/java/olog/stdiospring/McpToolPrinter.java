package olog.stdiospring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpToolPrinter implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(McpToolPrinter.class);

  private final List<McpSyncClient> clients;
  private final ObjectMapper mapper =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  public McpToolPrinter(List<McpSyncClient> clients) {
    this.clients = clients;
  }

  @Override
  public void run(String... args) {
    log.info("Found {} SYNC MCP client(s).", clients.size());

    for (McpSyncClient c : clients) {
      log.info("== SYNC MCP client: {}", c.getClass().getName());

      McpSchema.ListToolsResult res = c.listTools();
      if (res == null || res.tools() == null || res.tools().isEmpty()) {
        log.warn("   (no tools)");
        continue;
      }

      for (var t : res.tools()) {
        log.info("   - tool: {} — {}", t.name(), t.description());

        // 1) Dump the tool input schema (pretty JSON if possible)
        var schema = t.inputSchema();
        if (schema != null) {
          try {
            String pretty = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(schema);
            log.info("       inputSchema:\n{}", pretty);
          } catch (Exception e) {
            // Fallback if schema isn't directly serializable
            log.info("       inputSchema: {}", schema);
          }
        } else {
          log.warn("       (no input schema)");
        }

        // 2) Quick self-check for the 'search' tool: query_body MUST be an object
        if ("search".equalsIgnoreCase(t.name()) && schema != null) {
          try {
            JsonNode root = mapper.valueToTree(schema);
            JsonNode props = root.path("properties");
            JsonNode queryBody = props.path("query_body");

            String type = queryBody.path("type").asText("");
            boolean hasProps = queryBody.has("properties");

            log.info("       [check] search.query_body.type='{}' hasProperties={}", type, hasProps);

            if (!"object".equals(type) && !hasProps) {
              log.warn("       [check] query_body is NOT typed as an object; "
                  + "LLMs may send it as a string (fix your params to Map<String,Value>/Value+JsonSchema).");
            }
          } catch (Exception e) {
            log.warn("       [check] could not inspect schema for 'search': {}", e.toString());
          }
        }
      }
    }
  }
}

