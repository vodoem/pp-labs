package ru.rsreu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в CLI систему управления параллельными задачами!");
        System.out.println("Доступные команды: start <points>, exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "start":
                        if (parts.length != 2) {
                            System.out.println("Использование: start <points>");
                            break;
                        }
                        int points = Integer.parseInt(parts[1]);
                        int taskId = manager.startParallelTask(points);
                        System.out.println("Идентификатор задачи: " + taskId);
                        break;

                    default:
                        System.out.println("Неизвестная команда. Попробуйте снова.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: неверный числовой формат.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
}
