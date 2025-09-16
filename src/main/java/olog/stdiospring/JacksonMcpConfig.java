package olog.stdiospring;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;   // ⬅️ add
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonMcpConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer mcpJsonToTextCustomizer() {
    return builder -> {
      // Register our MCP Content JSON→text fallback
      builder.modules(new McpContentJsonToTextModule());
      // Register support for java.time (Instant, LocalDateTime, etc.)
      builder.modules(new JavaTimeModule());                     // ⬅️ add
      // Be lenient with future/unknown fields from MCP servers
      builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    };
  }
}

