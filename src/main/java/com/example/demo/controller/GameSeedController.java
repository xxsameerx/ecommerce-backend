package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.model.FreeToGameDto;
import com.example.demo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class GameSeedController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final Random random = new Random();

    @PostMapping("/seed-games")
    public Map<String, Object> seedGames() {
        String url = "https://www.freetogame.com/api/games";

        FreeToGameDto[] results = restTemplate.getForObject(url, FreeToGameDto[].class);

        List<Game> allGames = new ArrayList<>();

        double[] priceOptionsINR = { 499, 799, 999, 1499, 1999, 2499, 2999, 3999 };
        double[] discountFactors = { 0.5, 0.6, 0.7, 0.75, 0.8 };

        if (results != null) {
            for (FreeToGameDto dto : results) {
                if (gameRepository.existsByTitle(dto.title))
                    continue;

                Game game = new Game();
                game.setTitle(dto.title);
                game.setGenre(dto.genre);
                game.setPlatform(dto.platform);
                game.setPublisher(dto.publisher);
                game.setImageUrl(dto.thumbnail);
                game.setDescription(dto.short_description);
                game.setReleaseDate(dto.release_date);

                game.setRating(Math.round((3.0 + random.nextDouble() * 2.0) * 10.0) / 10.0);

                double price = priceOptionsINR[random.nextInt(priceOptionsINR.length)];
                game.setPrice(price);

                if (random.nextDouble() < 0.4) {
                    double discount = Math.round(price * discountFactors[random.nextInt(discountFactors.length)]);
                    game.setDiscountPrice(discount);
                }

                game.setStock(random.nextInt(200));
                allGames.add(game);
            }
        }

        gameRepository.saveAll(allGames);

        return Map.of("message", "Seeded real games from FreeToGame", "count", allGames.size());
    }
}