package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @GetMapping
    public Page<Game> getAllGames(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {

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

        int start = Math.min(page * size, games.size());
        int end = Math.min(start + size, games.size());
        List<Game> pageContent = games.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(
            pageContent, PageRequest.of(page, size), games.size()
        );
    }

    @GetMapping("/{id}")
    public Game getGameById(@PathVariable Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
    }
}