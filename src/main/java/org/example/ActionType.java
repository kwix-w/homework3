package org.example;

public enum ActionType {
    BASE_ATTACK("базовая атака"),
    SPECIAL_SKILL("применение особого умения или магии"),
    REST("пропуск хода ради восстановления сил");

    private final String rusName;

    ActionType(String rusName) {
        this.rusName = rusName;
    }

    public String getRusName() {
        return rusName;
    }
}
