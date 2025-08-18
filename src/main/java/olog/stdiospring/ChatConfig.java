package olog.stdiospring;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;   
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

  @Bean
  ChatClient chatClient(OllamaChatModel model,
                        ObjectProvider<ToolCallbackProvider> maybeMcpTools) {
    ChatClient.Builder b = ChatClient.builder(model);
    var mcpTools = maybeMcpTools.getIfAvailable();
    if (mcpTools != null) {
      b = b.defaultToolCallbacks(mcpTools.getToolCallbacks());
    }
    return b.build();
  }
}


