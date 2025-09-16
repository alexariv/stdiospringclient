package olog.stdiospring;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;   
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import org.springframework.ai.model.tool.ToolCallback;

@Configuration
public class ChatConfig {

  @Bean
  ChatClient chatClient(OllamaChatModel model,
                        ObjectProvider<ToolCallbackProvider> maybeMcpTools) {
    ChatClient.Builder b = ChatClient.builder(model);
    var mcpTools = maybeMcpTools.getIfAvailable();
    if (mcpTools != null) {
      List<ToolCallback> raw = mcpTools.getToolCallbacks();
      List<ToolCallback> safe = raw.stream()
          .map(SanitizeMcpToolCallback::new)
          .toList();
      b = b.defaultToolCallbacks(safe);
    }

    return b.build();
  }
}
