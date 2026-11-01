package com.example.bucket4jdemo.controller;

import io.github.bucket4j.Bucket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class TestController {

    private final Bucket bucket;

    public TestController(Bucket bucket) {
        this.bucket = bucket;
    }

    @GetMapping("/")
    public RedirectView swagger() {
        return new RedirectView("/swagger-ui/index.html");
    }

    @GetMapping("/api/test")
    public ResponseEntity<String> test() {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok("Запрос разрешён");
        }
        return ResponseEntity.status(429).body("Превышен лимит запросов");
    }

    @GetMapping("/api/bucket/available")
    public Map<String, Object> availableTokens() {
        return Map.of("availableTokens", bucket.getAvailableTokens());
    }

    @GetMapping("/api/bucket/estimate")
    public Map<String, Object> estimate(@RequestParam(defaultValue = "1") long tokens) {
        var probe = bucket.estimateAbilityToConsume(tokens);
        return Map.of(
                "tokensRequested", tokens,
                "canBeConsumed", probe.canBeConsumed(),
                "remainingTokens", probe.getRemainingTokens(),
                "nanosToWaitForRefill", probe.getNanosToWaitForRefill());
    }

    @GetMapping("/api/bucket/consume")
    public ResponseEntity<Map<String, Object>> consume(
            @RequestParam(defaultValue = "1") long tokens) {
        var probe = bucket.tryConsumeAndReturnRemaining(tokens);
        Map<String, Object> response = Map.of(
                "tokensRequested", tokens,
                "consumed", probe.isConsumed(),
                "remainingTokens", probe.getRemainingTokens(),
                "nanosToWaitForRefill", probe.getNanosToWaitForRefill());

        return probe.isConsumed()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(429).body(response);
    }

    @GetMapping("/api/bucket/consume-as-much-as-possible")
    public Map<String, Object> consumeAsMuchAsPossible() {
        long consumedTokens = bucket.tryConsumeAsMuchAsPossible();
        return Map.of(
                "consumedTokens", consumedTokens,
                "remainingTokens", bucket.getAvailableTokens());
    }

    @GetMapping("/api/bucket/add-tokens")
    public Map<String, Object> addTokens(@RequestParam(defaultValue = "1") long tokens) {
        bucket.addTokens(tokens);
        return Map.of("addedTokens", tokens, "availableTokens", bucket.getAvailableTokens());
    }

    @GetMapping("/api/bucket/reset")
    public Map<String, Object> reset() {
        bucket.reset();
        return Map.of("reset", true, "availableTokens", bucket.getAvailableTokens());
    }
}
