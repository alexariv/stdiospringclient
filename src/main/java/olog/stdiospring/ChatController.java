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
          You are an MCP agent for Elasticsearch.
          - If the user asks for documents (e.g., "most recent", "top 10", "sorted by <field>", "filter by ..."),
            CALL THE `search` tool with correct arguments that match its JSON schema.
          - Only use `get_shards` for shard/cluster info (never for document retrieval).
          - `esql` is for ES|QL queries; prefer `search` for plain NL document retrieval.
          Do NOT invent fields. `query_body` must be an object (not a string).
        """)
        .user(prompt)
        .call()
        .content();
    return Map.of("reply", reply);
  }
}
