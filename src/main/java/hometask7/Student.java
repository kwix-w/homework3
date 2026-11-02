package hometask7;

public record Student(int id, String name, double grade) {
    public Student {
        if (id <= 0) {
            throw new IllegalArgumentException("id должен быть положительным: " + id);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (Double.isNaN(grade) || grade < StudentRegistry.MIN_GRADE || grade > StudentRegistry.MAX_GRADE) {
            throw new IllegalArgumentException("Оценка должна быть от 0.0 до 10.0: " + grade);
        }
    }
}

