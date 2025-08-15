package olog.stdiospring;

import io.modelcontextprotocol.client.McpClient;           // for McpClient.SyncSpec
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {

  private static final Logger log = LoggerFactory.getLogger(CustomMcpSyncClientCustomizer.class);

  @Override
  public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {
    log.info("Applying MCP sync customizer for server '{}'", serverConfigurationName);

    // Match the example: set request timeout, and register change/logging consumers
    spec.requestTimeout(Duration.ofSeconds(30));

    spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
      log.info("[{}] tools changed (count={}):", serverConfigurationName, tools.size());
      tools.forEach(t -> log.info("   - {}", t.getName()));
    });

    spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
      log.info("[{}] resources changed (count={})", serverConfigurationName, resources.size());
    });

    spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
      log.info("[{}] prompts changed (count={})", serverConfigurationName, prompts.size());
    });

    spec.loggingConsumer((McpSchema.LoggingMessageNotification msg) -> {
      log.info("[{}] server log: {} - {}", serverConfigurationName, msg.getLevel(), msg.getMessage());
    });

    
  }
}
