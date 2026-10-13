package org.example;

public class Warrior extends Hero implements Castable, Restable {
    private final int armor;

    public Warrior(String name, int maxHealth, int baseAttack, int armor) {
        super(name, maxHealth, baseAttack);
        if (armor >= MIN_STAT_VALUE) {
            this.armor = armor;
        } else {
            this.armor = MIN_STAT_VALUE;
            System.out.println("Введено некорректное значение armor, установлено минимально возможное: " + MIN_STAT_VALUE);
        }
    }

    public int getArmor() {
        return armor;
    }

    @Override
    public void takeDamage(int damage) {
        int finalDamage = damage - this.armor;
        if (finalDamage < 1) {
            finalDamage = 1;
        }
        super.takeDamage(finalDamage);
    }

    @Override
    public void attack(Hero target) {
        if (target == null) {
            return;
        }
        System.out.println(getName() + " наносит базовый удар по " + target.getName() + " на " + getBaseAttack() + " урона!");
        target.takeDamage(getBaseAttack());
    }

    @Override
    public ActionType makeTurn(Hero target) {
        if (canCast()) {
            castSpecialSkill(target);
            return ActionType.SPECIAL_SKILL;
        } else if (needsRest()) {
            rest();
            return ActionType.REST;
        } else {
            attack(target);
            return ActionType.BASE_ATTACK;
        }
    }

    @Override
    public void castSpecialSkill(Hero target) {
        if (target == null) {
            return;
        }
        if (canCast()) {
            int damage = getBaseAttack() + (this.armor * 2);
            System.out.println(getName() + " применяет особый навык [Удар щитом] по " + target.getName() + " на " + damage + " урона!");
            target.takeDamage(damage);
        } else {
            System.out.println("Воин попытался ударить щитом, но потерял равновесие!");
        }
    }

    @Override
    public boolean canCast() {
        return getHealth() < (getMaxHealth() * 0.5);
    }

    @Override
    public void rest() {
        int healAmount = this.armor * 2;
        heal(healAmount);
        System.out.println(getName() + " использует [Второе дыхание] и восстанавливает " + healAmount + " здоровья!");
    }

    @Override
    public boolean needsRest() {
        return getHealth() < (getMaxHealth() * 0.15);
    }
}