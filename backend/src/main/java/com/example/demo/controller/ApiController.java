package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    public record DeleteRequest(List<String> paths) {}
    public record DeleteResult(String path, boolean deleted, String reason) {}
    public record DeleteResponse(List<DeleteResult> results) {}

    @PostMapping("/delete")
    public ResponseEntity<DeleteResponse> delete(@RequestBody DeleteRequest req) {
        Path root = Paths.get(allowedRoot).toAbsolutePath().normalize();
        List<DeleteResult> results = new ArrayList<>();
        List<String> paths = req.paths() == null ? List.of() : req.paths();
        for (String p : paths) {
            results.add(deleteOne(root, p));
        }
        return ResponseEntity.ok(new DeleteResponse(results));
    }

    private DeleteResult deleteOne(Path root, String raw) {
        if (raw == null || raw.isBlank()) {
            return new DeleteResult(raw, false, "empty path");
        }
        try {
            Path target = root.resolve(raw).normalize();
            if (!target.startsWith(root)) {
                return new DeleteResult(raw, false, "outside allowed root");
            }
            if (Files.isSymbolicLink(target)) {
                return new DeleteResult(raw, false, "symlink refused");
            }
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return new DeleteResult(raw, false, "not found");
            }
            if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                return new DeleteResult(raw, false, "not a regular file");
            }
            Files.delete(target);
            return new DeleteResult(raw, true, "deleted");
        } catch (IOException e) {
            return new DeleteResult(raw, false, "io error: " + e.getMessage());
        } catch (RuntimeException e) {
            return new DeleteResult(raw, false, "error: " + e.getMessage());
        }
    }
}
