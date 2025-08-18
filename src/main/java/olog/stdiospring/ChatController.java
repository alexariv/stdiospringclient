package olog.stdiospring;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatClient chat;          // with MCP tools
  private final OllamaChatModel model;    // raw model (no tools)

  public ChatController(ChatClient chat, OllamaChatModel model) {
    this.chat = chat;
    this.model = model;
  }

  @PostMapping
  public Map<String, Object> chat(@RequestBody Map<String, Object> body) {
    String prompt = String.valueOf(body.getOrDefault("prompt", ""));
    if (prompt.isBlank()) return Map.of("error", "prompt is required");
    var reply = chat.prompt(prompt).call().content();
    return Map.of("reply", reply);
  }

  @PostMapping("/no-tools")
  public Map<String, Object> chatNoTools(@RequestBody Map<String, Object> body) {
    String prompt = String.valueOf(body.getOrDefault("prompt", ""));
    if (prompt.isBlank()) return Map.of("error", "prompt is required");

    long t0 = System.currentTimeMillis();
    var reply = ChatClient
        .builder(model)   // build a plain client with NO tool callbacks
        .build()
        .prompt(prompt)
        .call()
        .content();
    long ms = System.currentTimeMillis() - t0;

    return Map.of("reply", reply, "elapsedMs", ms);
  }
}


