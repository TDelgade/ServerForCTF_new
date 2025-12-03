package com.example.demo.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.demo.service.UsersService;
import com.example.demo.service.PromoService;
import com.example.demo.Task;
import com.example.demo.service.TaskService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsersService usersService;
    private final PromoService promoService;
    private final TaskService taskService;

    public DataInitializer(UsersService usersService, PromoService promoService, TaskService taskService) {
        this.usersService = usersService;
        this.promoService = promoService;
        this.taskService = taskService;
    }

    @Override
    public void run(String... args) {

        // --- Пользователи ---
        String[][] defaultUsers = {
                {"КиссАС", "123456"},
                {"User1", "pass111"},
                {"User2", "pass222"},
                {"User3", "pass333"},
                {"User4", "pass444"}
        };

        for (String[] data : defaultUsers) {
            String login = data[0];
            String password = data[1];

            if (usersService.getUserByLogin(login).isEmpty()) {
                usersService.createOrUpdateUser(login, password, 0, 0);
                System.out.println("Добавлен новый пользователь: " + login);
            } else {
                System.out.println("Пользователь уже существует, пропускаем: " + login);
            }
        }

        // --- Промокоды ---
        String[][] defaultPromos = {
                {"MINUS200", "-200"},
                {"FREE50", "50"},
                {"FREE100", "100"}
        };

        for (String[] data : defaultPromos) {
            String code = data[0];
            int points = Integer.parseInt(data[1]);

            if (promoService.getPromoByName(code).isEmpty()) {
                promoService.createPromo(code, points);
                System.out.println("Добавлен новый промокод: " + code + " (" + points + " баллов)");
            } else {
                System.out.println("Промокод уже существует, пропускаем: " + code);
            }
        }

        // --- Задания ---
        String[][] defaultTasks = {
                {"Задание 1", "100"},
                {"Задание 2", "150"},
                {"Задание 3", "200"},
                {"Задание 4", "250"},
                {"Задание 5", "300"}
        };

        for (String[] data : defaultTasks) {
            String title = data[0];
            int points = Integer.parseInt(data[1]);

            if (taskService.getTaskByTitle(title).isEmpty()) {
                Task task = new Task();
                task.setTitle(title);
                task.setPoints(points);
                task.setSolved(false);
                taskService.createTask(task); // Создание через TaskService
                System.out.println("Добавлено новое задание: " + title + " (" + points + " баллов)");
            } else {
                System.out.println("Задание уже существует, пропускаем: " + title);
            }
        }
    }
}

