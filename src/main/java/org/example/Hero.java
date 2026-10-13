package org.example;

public abstract class Hero {
    public static final int MIN_STAT_VALUE = 1;

    private final String name;
    private final int maxHealth;
    private final int baseAttack;
    private int health;

    protected Hero(String name, int maxHealth, int baseAttack) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "Безымянный герой";
        } else {
            this.name = name;
        }

        if (maxHealth < MIN_STAT_VALUE) {
            System.out.println("Введено некорректное значение maxHealth, установлено минимально возможное: " + MIN_STAT_VALUE);
            this.maxHealth = MIN_STAT_VALUE;
        } else {
            this.maxHealth = maxHealth;
        }
        this.health = this.maxHealth;

        if (baseAttack < MIN_STAT_VALUE) {
            System.out.println("Введено некорректное значение baseAttack, установлено минимально возможное: " + MIN_STAT_VALUE);
            this.baseAttack = MIN_STAT_VALUE;
        } else {
            this.baseAttack = baseAttack;
        }
    }

    public String getName() {
        return name;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getHealth() {
        return health;
    }

    public abstract void attack(Hero target);

    public abstract ActionType makeTurn(Hero target);

    public void takeDamage(int damage) {
        if (damage <= 0) {
            return;
        }

        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            System.out.println(this.name + " пал в бою!");
        }
    }

    public void heal(int amount) {
        if (amount <= 0) {
            return;
        }

        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    public boolean isAlive() {
        return this.health > 0;
    }
}