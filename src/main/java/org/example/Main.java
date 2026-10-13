package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Добро пожаловать на Арену Героев! ===");

        Hero fighter1 = createHero(scanner, 1);
        System.out.println();
        Hero fighter2 = createHero(scanner, 2);
        System.out.println();

        Arena arena = new Arena(fighter1, fighter2);
        arena.startTournament();

        scanner.close();
    }

    private static Hero createHero(Scanner scanner, int heroNumber) {
        System.out.println("Создание бойца #" + heroNumber + ":");
        System.out.println("Выберите класс персонажа: 1 — Воин, 2 — Маг");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера после считывания числа

        System.out.print("Введите имя героя: ");
        String name = scanner.nextLine();

        System.out.print("Введите максимальное здоровье (maxHealth): ");
        int maxHealth = scanner.nextInt();

        System.out.print("Введите базовую силу атаки (baseAttack): ");
        int baseAttack = scanner.nextInt();

        if (choice == 1) {
            System.out.print("Введите броню (armor): ");
            int armor = scanner.nextInt();
            return new Warrior(name, maxHealth, baseAttack, armor);
        } else {
            System.out.print("Введите максимальный запас маны (maxMana): ");
            int maxMana = scanner.nextInt();
            return new Mage(name, maxHealth, baseAttack, maxMana);
        }
    }
}