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
          - Never quote JSON: arrays as arrays, objects as objects.
          - search args: { index: string, query_body: object, fields?: string[] }.
          Put query/size/from/sort/aggs INSIDE query_body. Don’t invent params.
          - Use get_mappings if unsure of field names. Use esql only for ES|QL. get_shards is not for docs.
          Examples: fields:["*"] yes  fields:"[\"*\"]" no   query_body:{...} yes  query_body:"{...}" no
        """)
        .user(prompt)
        .call()
        .content();
    return Map.of("reply", reply);
  }
}
