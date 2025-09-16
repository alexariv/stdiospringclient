package olog.stdiospring;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
  private final ChatClient chat;
  public ChatController(ChatClient chat) { this.chat = chat; }

  @PostMapping
  public Map<String, Object> chat(@RequestBody Map<String, Object> body) {
    String prompt = String.valueOf(body.getOrDefault("prompt", ""));
    if (prompt.isBlank()) return Map.of("error", "prompt is required");
    var reply = chat
        .prompt()
        .system("""
        You are an MCP agent for Elasticsearch. Follow tool schemas exactly.
        Never quote JSON. Arrays as arrays, objects as objects.
        search arguments: { index: string, query_body: object, fields?: string[] }.
        Put query/size/from/sort/aggs/_source inside query_body.
        list_indices needs { index_pattern: string }.
        get_mappings needs { index: string }.
        esql needs { query: string }.
        get_shards takes { index?: string }
        """)
        .user(prompt)
        .call()
        .content();
    return Map.of("reply", reply);
  }
}
