package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @GetMapping
    public List<Game> getAllGames(@RequestParam(required = false) String search,
                                   @RequestParam(required = false) String genre) {
        List<Game> games = gameRepository.findAll();
        if (search != null && !search.isBlank()) {
            games = games.stream()
                .filter(g -> g.getTitle().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        if (genre != null && !genre.isBlank() && !genre.equalsIgnoreCase("All")) {
            games = games.stream()
                .filter(g -> g.getGenre().equalsIgnoreCase(genre))
                .toList();
        }
        return games;
    }

    @GetMapping("/{id}")
    public Game getGameById(@PathVariable Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
    }
}