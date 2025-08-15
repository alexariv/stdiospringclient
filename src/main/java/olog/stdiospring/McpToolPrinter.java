package olog.stdiospring;

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

  public McpToolPrinter(List<McpSyncClient> clients) {
    this.clients = clients;
  }

  @Override
  public void run(String... args) {
    log.info("Found {} SYNC MCP client(s).", clients.size());

    for (McpSyncClient c : clients) {
      log.info("== SYNC MCP client: {}", c.getClass().getName());

      // listTools() -> ListToolsResult (record), use .tools() accessor
      McpSchema.ListToolsResult res = c.listTools();
      if (res != null && res.tools() != null) {
        res.tools().forEach(t -> log.info("   - tool: {} — {}", t.name(), t.description()));
        log.warn("   (no tools)");
      }
    }
  }
}
