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
        ALWAYS USE A TOOL. 
        You are an MCP agent for Elasticsearch. Follow tool schemas exactly.
        Never quote JSON. Arrays as arrays, objects as objects.
        search arguments: { index: string, query_body: object, fields?: string[] }.
        Put query/size/from/sort/aggs/_source inside query_body.
        list_indices needs { index_pattern: string }.
        get_mappings needs { index: string }.
        esql needs { query: string }.
        get_shards takes { index?: string }
this is the elastic mapping 
{
  "settings": { "number_of_shards": 1, "number_of_replicas": 1 },
  "mappings": {
    "dynamic": true,
    "dynamic_templates": [
      {
        "all_at_string_as_keyword": {
          "match": "@*",
          "match_mapping_type": "string",
          "mapping": { "type": "keyword", "ignore_above": 256 }
        }
      }
    ],
    "properties": {
      "@id":        { "type": "keyword" },
      "@owner":     { "type": "keyword" },
      "@source":    { "type": "ip" },
      "@version":   { "type": "integer" },
      "@level":     { "type": "keyword" },
      "@state":     { "type": "keyword" },

      "@createdDate": {
        "type": "date",
        "format": "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSSXXX||yyyy-MM-dd'T'HH:mm:ssXXX||yyyy-MM-dd'T'HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ssZ"
      },
      "@modifiedDate": {
        "type": "date",
        "format": "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSSXXX||yyyy-MM-dd'T'HH:mm:ssXXX||yyyy-MM-dd'T'HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ssZ"
      },
      "@eventStart": {
        "type": "date",
        "format": "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSSXXX||yyyy-MM-dd'T'HH:mm:ssXXX||yyyy-MM-dd'T'HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ssZ"
      },
      "@eventEnd": {
        "type": "date",
        "format": "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSSXXX||yyyy-MM-dd'T'HH:mm:ssXXX||yyyy-MM-dd'T'HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ssZ"
      },

      "description": { "type": "text", "fields": { "raw": { "type": "keyword", "ignore_above": 2048 } } },

      "logbooks": {
        "properties": {
          "logbook": {
            "properties": {
              "@owner": { "type": "keyword" },
              "@name":  { "type": "keyword" },
              "@state": { "type": "keyword" },
              "id":     { "type": "keyword" }
            }
          }
        }
      },

      "tags": {
        "properties": {
          "tag": {
            "properties": {
              "@name":  { "type": "keyword" },
              "@state": { "type": "keyword" },
              "id":     { "type": "keyword" }
            }
          }
        }
      },

      "properties": {
        "properties": {
          "property": {
            "properties": {
              "@id":   { "type": "keyword" },
              "@name": { "type": "keyword" },
              "attributes": {
                "properties": {
                  "entry": {
                    "properties": {
                      "key":   { "type": "keyword" },
                      "value": { "type": "keyword", "ignore_above": 2048 }
                    }
                  }
                }
              }
            }
          }
        }
      },

      "attachments": {
        "properties": {
          "attachment": {
            "properties": {
              "contentType": { "type": "keyword" },
              "fileName":    { "type": "keyword" },
              "fileSize":    { "type": "long" }
            }
          }
        }
      }
    }
  }
}

        """)
        .user(prompt)
        .call()
        .content();
    return Map.of("reply", reply);
  }
}
