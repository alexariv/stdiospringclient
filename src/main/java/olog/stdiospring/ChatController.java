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
        Your priority is to always return a ranked list of relative log entries you retrieved from the database.
        EVERY SEARCH REQUEST:
        1. Understand the user's request
        2. When needed use the get_mappings tool for parameters.
        2. Build Query with correct parameters.
        3. Execute tool immediately
        4. Display the retrieved data with the properties @id # from the successfully query execution.
        5. First you MUST return the ranked list then you could contextualize the data in a different section below. 
        CRITICAL:
        You MUST actually call the search tool with every query request.
        Show results from actual execution, not hypothetical results.
        Execute the tool for every request. If it fails, fix and retry with the correct format.
        NEVER use nested and always start with query. 
        Do not use description.raw.
        """)
        .user(prompt)
        .call()
        .content();
    return Map.of("reply", reply);
  }
}
