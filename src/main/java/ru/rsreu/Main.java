package ru.rsreu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в CLI систему управления задачами!");
        System.out.println("Доступные команды: start <epsilon>, stop <id>, await <id>, exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");

            if (parts.length == 0) continue;

            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "start":
                        if (parts.length != 2) {
                            System.out.println("Использование: start <epsilon>");
                            break;
                        }
                        double epsilon = Double.parseDouble(parts[1]);
                        int taskId = manager.startNewTask(epsilon);
                        System.out.println("Идентификатор задачи: " + taskId);
                        break;

                    case "stop":
                        if (parts.length != 2) {
                            System.out.println("Использование: stop <id>");
                            break;
                        }
                        int stopId = Integer.parseInt(parts[1]);
                        manager.stopTask(stopId);
                        break;

                    case "await":
                        if (parts.length != 2) {
                            System.out.println("Использование: await <id>");
                            break;
                        }
                        int awaitId = Integer.parseInt(parts[1]);
                        manager.awaitTask(awaitId);
                        break;

                    case "exit":
                        manager.shutdownAll();
                        System.out.println("Выход из программы.");
                        return;

                    default:
                        System.out.println("Неизвестная команда. Попробуйте снова.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный числовой формат.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
}
