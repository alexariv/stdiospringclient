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
        You are an MCP agent for Elasticsearch. Follow  the tool schemas.
        Your priority is to always return a ranked list of relative log entries you retrieved from the database.
        You must always include the properties @id for each retrival.
        Use get_mappings to get the mapping to build the query with the right parameters. 
        After you have done this then you could contextualize and analyze the retrivals. 
        
        CRITICAL EXECUTION REQUIREMENT:
        You MUST actually call the search tool with every query request. 
        If the query fails, read the error, fix it, and try again.
        Show results from actual execution, not hypothetical results.

        WORKFLOW FOR EVERY SEARCH REQUEST:
        1. Understand the user's request
        2. Construct the Elasticsearch query
        3. IMMEDIATELY call the search tool with your constructed query
        4. Present the results to the user (query results with id) & then your cotextualization.
        """)
        .user(prompt)
        .call()
        .content();
    return Map.of("reply", reply);
  }
}
