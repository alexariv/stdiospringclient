package olog.stdiospring;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {

  @GetMapping("/ping")
  public Map<String, Object> ping() {
    return Map.of("ok", true, "ts", System.currentTimeMillis());
  }
}
