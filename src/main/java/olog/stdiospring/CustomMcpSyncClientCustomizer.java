package olog.stdiospring;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import olog.stdiospring.McpContentJsonToTextModule;


@Component
public class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {

  private static final Logger log = LoggerFactory.getLogger(CustomMcpSyncClientCustomizer.class);

  @Override
  public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {
    log.info("Applying MCP sync customizer for server '{}'", serverConfigurationName);

    spec.requestTimeout(Duration.ofSeconds(30));

    spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
      log.info("[{}] tools changed (count={}):", serverConfigurationName, tools.size());
      for (var t : tools) {
        // Prefer title() if available, else name()
        String display = (t.title() != null && !t.title().isEmpty()) ? t.title() : t.name();
        log.info("   - {} (name='{}')", display, t.name());
      }
    });

    spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
      log.info("[{}] resources changed (count={})", serverConfigurationName, resources.size());
      for (var r : resources) {
        log.info("   - {}", r.name());
      }
    });

    spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
      log.info("[{}] prompts changed (count={})", serverConfigurationName, prompts.size());
      for (var p : prompts) {
        String display = (p.title() != null && !p.title().isEmpty()) ? p.title() : p.name();
        log.info("   - {} (name='{}')", display, p.name());
      }
    });

    spec.loggingConsumer((McpSchema.LoggingMessageNotification msg) -> {
      String loggerName = (msg.logger() != null && !msg.logger().isEmpty()) ? msg.logger() : "server";
      log.info("[{}] server log: {} - {} - {}", serverConfigurationName, msg.level(), loggerName, msg.data());
    });
  }
}

