package org.example;

public class Arena {
    private final Hero fighter1;
    private final Hero fighter2;
    private int roundCounter;

    public Arena(Hero fighter1, Hero fighter2) {
        if (fighter1 == null || fighter2 == null) {
            throw new IllegalArgumentException("Бойцы не могут быть null!");
        }
        this.fighter1 = fighter1;
        this.fighter2 = fighter2;
        this.roundCounter = 0;
    }

    public void startTournament() {
        System.out.println("=== Битва начинается: " + fighter1.getName() + " против " + fighter2.getName() + " ===");

        while (fighter1.isAlive() && fighter2.isAlive()) {
            roundCounter++;
            System.out.println("\n--- Раунд " + roundCounter + " ---");

            Hero attacker;
            Hero defender;

            if (Math.random() < 0.5) {
                attacker = fighter1;
                defender = fighter2;
            } else {
                attacker = fighter2;
                defender = fighter1;
            }

            executeTurn(attacker, defender);

            if (!defender.isAlive()) {
                break;
            }

            executeTurn(defender, attacker);

            if (!attacker.isAlive()) {
                break;
            }

            printFighterStatus(fighter1);
            printFighterStatus(fighter2);
        }

        Hero winner = fighter1.isAlive() ? fighter1 : fighter2;
        System.out.println("\n=== Бой окончен! Победитель: " + winner.getName() + " ===");
    }

    private void executeTurn(Hero attacker, Hero defender) {
        ActionType action = attacker.makeTurn(defender);
        System.out.println("Игрок " + attacker.getName() + " совершил действие: " + action.getRusName());
    }

    private void printFighterStatus(Hero hero) {
        StringBuilder status = new StringBuilder();
        status.append("Статус ").append(hero.getName()).append(": ")
                .append(hero.getHealth()).append("/").append(hero.getMaxHealth()).append(" HP");

        if (hero instanceof Mage mage) {
            status.append(" | ").append(mage.getMana()).append("/").append(mage.getMaxMana()).append(" MP");
        } else if (hero instanceof Warrior warrior) {
            status.append(" | Броня: ").append(warrior.getArmor());
        }

        System.out.println(status);
    }
}