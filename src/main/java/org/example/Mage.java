package org.example;

public class Mage extends Hero implements Castable, Restable {
    private final int maxMana;
    private int mana;

    public Mage(String name, int maxHealth, int baseAttack, int maxMana) {
        super(name, maxHealth, baseAttack);
        if (maxMana >= MIN_STAT_VALUE) {
            this.maxMana = maxMana;
        } else {
            this.maxMana = MIN_STAT_VALUE;
            System.out.println("Введено некорректное значение maxMana, установлено минимально возможное: " + MIN_STAT_VALUE);
        }
        this.mana = this.maxMana;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void castSpecialSkill(Hero target) {
        if (target == null) {
            return;
        }

        if (canCast()) {
            this.mana -= 25;
            System.out.println(getName() + " обрушивает заклинание [Огненная глыба] на " + target.getName() + "!");
            target.takeDamage(getBaseAttack() * 3);
        } else {
            System.out.println(getName() + " попытался сотворить заклинание [Огненная глыба], но ему не хватило маны!");
        }
    }

    @Override
    public boolean canCast() {
        return this.mana >= 25;
    }

    @Override
    public void attack(Hero target) {
        if (target == null) {
            return;
        }

        if (this.mana >= 10) {
            this.mana -= 10;
            System.out.println(getName() + " использует магический выстрел на " + target.getName() + "!");
            target.takeDamage(getBaseAttack() * 2);
        } else {
            this.mana += 5;
            if (this.mana > this.maxMana) {
                this.mana = this.maxMana;
            }
            System.out.println(getName() + " наносит удар посохом по " + target.getName() + " и восстанавливает 5 маны!");
            target.takeDamage(getBaseAttack() / 2);
        }
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
    public void rest() {
        this.mana = this.maxMana;
        heal(getBaseAttack());
        System.out.println(getName() + " использует исцеляющую медитацию, полностью восстанавливая ману и исцеляясь!");
    }

    @Override
    public boolean needsRest() {
        return this.mana == 0 || getHealth() < (getMaxHealth() * 0.3);
    }
}