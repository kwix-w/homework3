package hometask8;

import java.util.List;
import java.util.Objects;

public final class ValidationResult {
    private final List<ValidationError> errors;

    public ValidationResult(List<ValidationError> errors) {
        Objects.requireNonNull(errors, "Errors list cannot be null");
        this.errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
