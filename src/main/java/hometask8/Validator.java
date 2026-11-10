package hometask8;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Validator {

    private Validator() {
        // Приватный конструктор для утилитного класса
    }

    public static ValidationResult validate(Object object) {
        Objects.requireNonNull(object, "Validated object must not be null");

        List<ValidationError> errors = new ArrayList<>();
        Class<?> currentClass = object.getClass();

        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                validateField(object, field, errors);
            }
            currentClass = currentClass.getSuperclass();
        }

        return new ValidationResult(errors);
    }

    private static void validateField(Object target, Field field, List<ValidationError> errors) {
        if (!field.trySetAccessible()) {
            throw new ValidationConfigurationException(
                    "Cannot access field '" + field.getName() + "' in class " + field.getDeclaringClass().getName()
            );
        }

        final Object value;
        try {
            value = field.get(target);
        } catch (IllegalAccessException e) {
            throw new ValidationConfigurationException(
                    "Failed to read field '" + field.getName() + "' in class " + field.getDeclaringClass().getName(), e
            );
        }

        if (field.isAnnotationPresent(NotNull.class)) {
            validateNotNull(field, value, errors);
        }
        if (field.isAnnotationPresent(Size.class)) {
            validateSize(field, value, errors);
        }
        if (field.isAnnotationPresent(Range.class)) {
            validateRange(field, value, errors);
        }
        if (field.isAnnotationPresent(Email.class)) {
            validateEmail(field, value, errors);
        }
    }

    private static void validateNotNull(Field field, Object value, List<ValidationError> errors) {
        NotNull annotation = field.getAnnotation(NotNull.class);
        if (!field.getType().isPrimitive() && value == null) {
            errors.add(new ValidationError(
                    field.getName(),
                    NotNull.class.getSimpleName(),
                    annotation.message()
            ));
        }
    }

    private static void validateSize(Field field, Object value, List<ValidationError> errors) {
        Size annotation = field.getAnnotation(Size.class);
        Class<?> fieldType = field.getType();

        if (!CharSequence.class.isAssignableFrom(fieldType)) {
            throw new ValidationConfigurationException(
                    "@Size is not supported on field '" + field.getName() + "' of type " + fieldType.getName()
            );
        }

        int min = annotation.min();
        int max = annotation.max();
        if (min < 0 || min > max) {
            throw new ValidationConfigurationException(
                    "Invalid @Size bounds on field '" + field.getName() + "': min=" + min + ", max=" + max
            );
        }

        if (value == null) {
            return;
        }

        int length = ((CharSequence) value).length();
        if (length < min || length > max) {
            errors.add(new ValidationError(
                    field.getName(),
                    Size.class.getSimpleName(),
                    annotation.message()
            ));
        }
    }

    private static void validateRange(Field field, Object value, List<ValidationError> errors) {
        Range annotation = field.getAnnotation(Range.class);
        Class<?> fieldType = field.getType();

        if (!isNumericType(fieldType)) {
            throw new ValidationConfigurationException(
                    "@Range is not supported on non-numeric field '" + field.getName() + "' of type " + fieldType.getName()
            );
        }

        double min = annotation.min();
        double max = annotation.max();
        if (Double.isNaN(min) || Double.isNaN(max) || Double.isInfinite(min) || Double.isInfinite(max) || min > max) {
            throw new ValidationConfigurationException(
                    "Invalid @Range bounds on field '" + field.getName() + "': min=" + min + ", max=" + max
            );
        }

        if (value == null) {
            return;
        }

        double numericValue = ((Number) value).doubleValue();
        if (Double.isNaN(numericValue) || numericValue < min || numericValue > max) {
            errors.add(new ValidationError(
                    field.getName(),
                    Range.class.getSimpleName(),
                    annotation.message()
            ));
        }
    }

    private static void validateEmail(Field field, Object value, List<ValidationError> errors) {
        Email annotation = field.getAnnotation(Email.class);
        Class<?> fieldType = field.getType();

        if (!CharSequence.class.isAssignableFrom(fieldType)) {
            throw new ValidationConfigurationException(
                    "@Email is not supported on field '" + field.getName() + "' of type " + fieldType.getName()
            );
        }

        if (value == null) {
            return;
        }

        String email = value.toString();
        if (!isValidEmail(email)) {
            errors.add(new ValidationError(
                    field.getName(),
                    Email.class.getSimpleName(),
                    annotation.message()
            ));
        }
    }

    private static boolean isNumericType(Class<?> type) {
        if (Number.class.isAssignableFrom(type)) {
            return true;
        }
        return type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class;
    }

    private static boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            return false;
        }
        for (int i = 0; i < email.length(); i++) {
            if (Character.isWhitespace(email.charAt(i))) {
                return false;
            }
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || email.indexOf('@', atIndex + 1) != -1) {
            return false;
        }

        String domainPart = email.substring(atIndex + 1);
        int dotIndex = domainPart.indexOf('.');
        return !domainPart.isEmpty() && dotIndex > 0 && dotIndex < domainPart.length() - 1;
    }
}