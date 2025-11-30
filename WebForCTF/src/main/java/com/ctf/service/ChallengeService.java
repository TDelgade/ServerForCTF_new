package com.ctf.service;

import com.ctf.model.Challenge;
import com.ctf.repository.ChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    // Добавить метод для инициализации заданий
    public void initializeChallenges() {
        // SQL Injection Challenge
        if (!challengeRepository.findByTitle("SQL Injection Basic").isPresent()) {
            Challenge sqliChallenge = new Challenge(
                    "SQL Injection Basic",
                    "Обойдите аутентификацию с помощью SQL инъекции. Найдите флаг в базе данных.",
                    "web",
                    100,
                    "easy",
                    "CTF{sql_1nj3ct10n_3asy_w1n}",
                    "Попробуйте использовать специальные символы в поле username",
                    "Изучите как работают SQL запросы и кавычки в условиях WHERE"
            );
            challengeRepository.save(sqliChallenge);
        }

        // Authentication Bypass Challenge
        if (!challengeRepository.findByTitle("Authentication Bypass").isPresent()) {
            Challenge authBypassChallenge = new Challenge(
                    "Authentication Bypass",
                    "Обойдите механизм аутентификации и получите доступ к административной панели.",
                    "web",
                    120,
                    "easy",
                    "CTF{auth_bypass_m4st3r_2024}",
                    "Проверьте разные способы хранения данных в браузере",
                    "Изучите куки, localStorage и параметры URL"
            );
            challengeRepository.save(authBypassChallenge);
        }

        // XSS Challenge
        if (!challengeRepository.findByTitle("XSS Challenge").isPresent()) {
            Challenge xssChallenge = new Challenge(
                    "XSS Challenge",
                    "Execute cross-site scripting attacks in the comment section to steal the flag.",
                    "web",
                    200,
                    "medium",
                    "CTF{XSS_MASTER_2024}", // ИЗМЕНЕНО: синхронизирован с фронтендом
                    "Попробуйте вставить HTML теги с JavaScript в комментарии",
                    "Изучите различные типы XSS payloads и event handlers"
            );
            challengeRepository.save(xssChallenge);
        }

        // CSRF Challenge
        if (!challengeRepository.findByTitle("CSRF Challenge").isPresent()) {
            Challenge csrfChallenge = new Challenge(
                    "CSRF Challenge",
                    "Perform Cross-Site Request Forgery attack to transfer funds without user consent.",
                    "web",
                    150,
                    "medium",
                    "CTF{csrf_vulnerable_2024}",
                    "Создайте страницу которая автоматически отправляет форму",
                    "Изучите как браузеры обрабатывают запросы между сайтами"
            );
            challengeRepository.save(csrfChallenge);
        }

        // Path Traversal Challenge
        if (!challengeRepository.findByTitle("Path Traversal").isPresent()) {
            Challenge pathTraversalChallenge = new Challenge(
                    "Path Traversal",
                    "Access files outside the web root directory using path traversal techniques.",
                    "web",
                    250,
                    "hard",
                    "CTF{path_traversal_master_2024}",
                    "Используйте последовательности для навигации по директориям",
                    "Изучите как операционные системы обрабатывают пути к файлам"
            );
            challengeRepository.save(pathTraversalChallenge);
        }
    }

    public Optional<Challenge> getChallengeByTitle(String title) {
        return challengeRepository.findByTitle(title);
    }

    public boolean validateFlagByChallengeName(String challengeName, String flag) {
        Optional<Challenge> challenge = challengeRepository.findByTitle(challengeName);
        return challenge.isPresent() && challenge.get().getFlag().equals(flag);
    }

    public boolean validateSqlInjection(String username, String password) {
        String vulnerableQuery = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        System.out.println("Executing vulnerable query: " + vulnerableQuery);

        // Нормализуем входные данные для проверки
        String normalizedUsername = username.trim().toLowerCase();
        String normalizedPassword = password.trim().toLowerCase();

        // Расширенная и более гибкая проверка SQL инъекций
        boolean isSqlInjection =
                // Классические инъекции в username
                normalizedUsername.contains("' or '1'='1") ||
                        normalizedUsername.contains("' or 1=1--") ||
                        normalizedUsername.contains("admin'--") ||
                        normalizedUsername.contains("' or 'a'='a") ||
                        normalizedUsername.contains("' or 'x'='x") ||
                        normalizedUsername.contains("' or ''='") ||
                        normalizedUsername.endsWith("'--") ||
                        normalizedUsername.contains("' union select") ||

                        // Инъекции в password
                        normalizedPassword.contains("' or '1'='1") ||
                        normalizedPassword.contains("' or 1=1--") ||

                        // Более гибкие проверки
                        username.contains("'") && (username.contains("or") || username.contains("OR")) ||
                        username.contains("--") ||
                        username.contains("/*") ||

                        // Проверяем обход аутентификации
                        (!username.equals("admin") && username.contains("'--")) ||
                        username.matches(".*'\\s*(OR|or)\\s*'.*'.*");

        System.out.println("SQL Injection detected: " + isSqlInjection);
        return isSqlInjection;
    }

    // Метод для проверки Path Traversal
    public boolean checkPathTraversal(String path) {
        // Уязвимая проверка пути
        if (path.contains("../") ||
                path.contains("..\\") ||
                path.contains("/etc/passwd") ||
                path.contains("/secret/") ||
                path.contains("flag.txt")) {
            return true;
        }
        return false;
    }

    // Метод для проверки XSS payload
    public boolean detectXSSPayload(String input) {
        // Простая проверка XSS векторов
        return input.contains("<script>") ||
                input.contains("javascript:") ||
                input.contains("onerror=") ||
                input.contains("onload=") ||
                input.contains("onclick=") ||
                input.contains("<img") ||
                input.contains("<svg") ||
                input.contains("alert(");
    }

    // Метод для проверки CSRF атаки
    public boolean validateCSRFAttempt(String amount, String targetAccount) {
        // Проверяем типичную CSRF атаку
        return "500".equals(amount) && "attacker_account".equals(targetAccount);
    }

    public List<Challenge> getChallengesByCategory(String category) {
        return challengeRepository.findByCategory(category);
    }

    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public Challenge saveChallenge(Challenge challenge) {
        return challengeRepository.save(challenge);
    }

    public void deleteChallenge(Long id) {
        challengeRepository.deleteById(id);
    }
}