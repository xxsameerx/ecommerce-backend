package com.example.demo.controller;

import com.example.demo.dto.RevenueResponse;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.model.Game;
import com.example.demo.model.User;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.PaymentOrderRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    // ---------- PRODUCT MANAGEMENT ----------

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

    // ---------- USER MANAGEMENT ----------

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            if (!request.getRole().equals("ADMIN") && !request.getRole().equals("USER")) {
                return ResponseEntity.badRequest().body("Role must be ADMIN or USER");
            }
            user.setRole(request.getRole());
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    // ---------- BUSINESS ANALYTICS ----------

    @GetMapping("/analytics/daily")
    public ResponseEntity<?> getDailyRevenue(@RequestParam String date) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format, use YYYY-MM-DD");
        }

        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = localDate.plusDays(1).atStartOfDay();

        Double revenue = paymentOrderRepository.sumRevenueBetween(start, end);
        Long orders = paymentOrderRepository.countOrdersBetween(start, end);

        return ResponseEntity.ok(new RevenueResponse(revenue, orders, date));
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<?> getMonthlyRevenue(@RequestParam int month, @RequestParam int year) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body("Month must be between 1 and 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        Double revenue = paymentOrderRepository.sumRevenueBetween(start, end);
        Long orders = paymentOrderRepository.countOrdersBetween(start, end);

        return ResponseEntity.ok(new RevenueResponse(revenue, orders, month + "/" + year));
    }

    @GetMapping("/analytics/yearly")
    public ResponseEntity<?> getYearlyRevenue(@RequestParam int year) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year + 1, 1, 1).atStartOfDay();

        Double revenue = paymentOrderRepository.sumRevenueBetween(start, end);
        Long orders = paymentOrderRepository.countOrdersBetween(start, end);

        return ResponseEntity.ok(new RevenueResponse(revenue, orders, String.valueOf(year)));
    }

    @GetMapping("/analytics/overall")
    public ResponseEntity<?> getOverallRevenue() {
        Double revenue = paymentOrderRepository.sumAllRevenue();
        Long orders = paymentOrderRepository.countAllOrders();

        return ResponseEntity.ok(new RevenueResponse(revenue, orders, "All Time"));
    }
}