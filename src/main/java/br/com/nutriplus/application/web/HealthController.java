package br.com.nutriplus.application.web;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController public class HealthController {
  @GetMapping("/healthz") public ResponseEntity<Map<String,Object>> health(){ return ResponseEntity.ok(Map.of("status","ok")); }
}