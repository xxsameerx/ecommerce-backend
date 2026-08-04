package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private GameRepository gameRepository;

    @PostMapping("/games")
    public ResponseEntity<?> addGame(@RequestBody Game game) {
        if (game.getTitle() == null || game.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body("Title is required");
        }
        if (game.getGenre() == null || game.getGenre().isBlank()) {
            return ResponseEntity.badRequest().body("Genre is required");
        }
        if (game.getPrice() == null || game.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Valid price is required");
        }

        boolean duplicate = gameRepository.existsByTitle(game.getTitle());
        if (duplicate) {
            return ResponseEntity.badRequest().body("A game with this title already exists");
        }

        Game saved = gameRepository.save(game);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        if (!gameRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Game not found");
        }
        gameRepository.deleteById(id);
        return ResponseEntity.ok("Game deleted successfully");
    }

    @GetMapping("/games")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameRepository.findAll());
    }
}