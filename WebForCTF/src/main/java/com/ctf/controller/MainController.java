package com.ctf.controller;

import com.ctf.model.User;
import com.ctf.service.UserService;
import com.ctf.service.ChallengeService;
import com.ctf.SessionRegistry;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private SessionRegistry sessionRegistry;

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // ==============================
    // HOME PAGE
    // ==============================
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String username = (String) session.getAttribute("username");

        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        model.addAttribute("username", username);

        return "index";
    }

    // ==============================
    // TOP-3 API
    // ==============================
    @GetMapping("/top3")
    @ResponseBody
    public List<Map<String, Object>> getTop3() {
        String backendUrl = "http://localhost:8081/top3";
        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<List> response = rest.getForEntity(backendUrl, List.class);
            return response.getBody();
        } catch (Exception e) {
            return List.of(Map.of("login", "ERROR", "points", 0));
        }
    }

    // ==============================
    // AUTH PAGE
    // ==============================
    @GetMapping("/auth")
    public String authPage(@RequestParam(value = "register", required = false) String register,
                           @RequestParam(value = "error", required = false) String error,
                           Model model) {

        model.addAttribute("isLogin", register == null);

        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }

        return "auth";
    }

    // ==============================
    // LOGIN
    // ==============================
    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {

        if (sessionRegistry.isUserActive(username)) {
            model.addAttribute("error", "Пользователь уже вошёл в систему в другой сессии");
            model.addAttribute("isLogin", true);
            return "auth";
        }

        try {
            logger.info("LOGIN ATTEMPT: username={} sessionID={}", username, session.getId());

            Map<String, String> payload = Map.of("login", username, "password", password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://backend:8080/api/auth/login",
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                session.setAttribute("username", username);
                session.setAttribute("isAuthenticated", true);

                sessionRegistry.registerSession(session.getId(), username);

                logger.info("LOGIN SUCCESS: username={} sessionID={}", username, session.getId());

                if ("admin".equalsIgnoreCase(username)) {
                    return "redirect:/admin-users.html";
                }

                return "redirect:/tests";
            } else {
                model.addAttribute("error", "Неверный логин или пароль");
                model.addAttribute("isLogin", true);
                return "auth";
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            model.addAttribute("error", "Неверный логин или пароль");
            model.addAttribute("isLogin", true);
            return "auth";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при входе: " + e.getMessage());
            model.addAttribute("isLogin", true);
            return "auth";
        }
    }

    // ==============================
    // CHECK USERNAME/EMAIL
    // ==============================
    @GetMapping("/check-username")
    @ResponseBody
    public String checkUsername(@RequestParam String username) {
        if (username == null || username.trim().length() < 3) return "invalid";
        return userService.usernameExists(username.trim()) ? "exists" : "available";
    }

    @GetMapping("/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) return "invalid";
        return userService.emailExists(email.trim().toLowerCase()) ? "exists" : "available";
    }

    // ==============================
    // USERS LIST
    // ==============================
    @GetMapping("/users")
    public String showUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    // ==============================
    // ADMIN PAGE
    // ==============================
    @GetMapping("/admin-users.html")
    public String adminUsers(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");

        if (!"admin".equalsIgnoreCase(username)) {
            return "redirect:/auth";
        }

        model.addAttribute("currentUser", username);
        return "admin-users";
    }

    // ==============================
    // LOGOUT
    // ==============================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        sessionRegistry.removeSession(session.getId());
        session.invalidate();
        return "redirect:/";
    }

    // ==============================
    // DEBUG PAGE
    // ==============================
    @GetMapping("/debug")
    public String debugPage(Model model) {

        String backendUrl = "http://backend:8080/debug/public/ping";
        RestTemplate restTemplate = new RestTemplate();

        String backendResponse;
        try {
            backendResponse = restTemplate.getForObject(backendUrl, String.class);
        } catch (Exception e) {
            backendResponse = "Ошибка при обращении к бэку: " + e.getMessage();
        }

        model.addAttribute("pingResponse", backendResponse);
        return "debug";
    }

    // ==============================
    // TESTS PAGE
    // ==============================
    @GetMapping("/tests")
    public String testPage(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (sessionRegistry.hasCompletedTest(username)) {
            return "redirect:/";
        }

        return "tests";
    }

    @PostMapping("/tests/complete")
    public ResponseEntity<Void> completeTest(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username != null) {
            sessionRegistry.markTestCompleted(username);
        }

        return ResponseEntity.ok().build();
    }

    // ==============================
    // CATEGORY ROUTES
    // ==============================
    @GetMapping("/category/{category}")
    public String showCategory(@PathVariable String category, Model model) {

        model.addAttribute("category", category);

        switch (category.toLowerCase()) {
            case "pwn": return "categories/pwn";
            case "web": return "categories/web";
            case "crypto": return "categories/crypto";
            default: return "redirect:/";
        }
    }

    // ==============================
    // WEB CHALLENGES
    // ==============================
    @GetMapping("/challenges/web")
    public String webChallengesOverview(Model model) {

        List<Map<String, Object>> challenges = List.of(
                Map.of("title", "SQL Injection Basic", "points", 100, "difficulty", "easy", "url", "/challenges/sqli"),
                Map.of("title", "Authentication Bypass", "points", 120, "difficulty", "easy", "url", "/challenges/auth-bypass"),
                Map.of("title", "XSS Challenge", "points", 200, "difficulty", "medium", "url", "/challenges/xss"),
                Map.of("title", "CSRF Challenge", "points", 150, "difficulty", "medium", "url", "/challenges/csrf"),
                Map.of("title", "Path Traversal", "points", 250, "difficulty", "hard", "url", "/challenges/path-traversal")
        );

        model.addAttribute("challenges", challenges);
        return "categories/web";
    }

    // ==============================
    // VALIDATE CHALLENGE FLAG
    // ==============================
    @PostMapping("/api/challenges/validate")
    @ResponseBody
    public Map<String, Object> validateChallengeFlag(@RequestBody Map<String, String> request) {

        try {
            String challengeName = request.get("challengeName");
            String flag = request.get("flag");

            boolean isValid = challengeService.validateFlagByChallengeName(challengeName, flag);

            if (isValid) {
                return Map.of("success", true, "message", "🎉 Флаг верный! Задание выполнено.");
            } else {
                return Map.of("success", false, "message", "❌ Неверный флаг. Попробуйте еще раз.");
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "❌ Ошибка сервера при проверке флага");
        }
    }

    // ==============================
    // ACTIVE SESSIONS
    // ==============================
    @GetMapping("/api/sessions")
    @ResponseBody
    public List<Map<String, String>> getActiveSessions() {
        return sessionRegistry.getActiveSessions().entrySet().stream()
                .map(e -> Map.of(
                        "sessionId", e.getKey(),
                        "username", e.getValue()
                ))
                .toList();
    }

}
