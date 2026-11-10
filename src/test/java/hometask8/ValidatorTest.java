package hometask8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    static class BaseEntity {
        @NotNull(message = "ID should not be null")
        private final String id;

        @NotNull(message = "Static field must be ignored")
        private static String ignoredStaticField = null;

        BaseEntity(String id) {
            this.id = id;
        }
    }

    static class User extends BaseEntity {
        @NotNull(message = "Name cannot be null")
        @Size(min = 2, max = 10, message = "Name must be 2..10 chars")
        private final String name;

        @NotNull(message = "Email is required")
        @Email(message = "Invalid email")
        private final CharSequence email;

        @Range(min = 18, max = 100, message = "Age must be 18..100")
        private final Integer age;

        @NotNull
        @Range(min = 0, max = 10)
        private final int primitiveRating;

        User(String id, String name, CharSequence email, Integer age, int primitiveRating) {
            super(id);
            this.name = name;
            this.email = email;
            this.age = age;
            this.primitiveRating = primitiveRating;
        }
    }

    @Test
    void validObjectReturnsValidResultAndEmptyList() {
        User user = new User("123", "Alice", new StringBuilder("alice@phystech.edu"), 19, 5);

        ValidationResult result = Validator.validate(user);

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validateNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Validator.validate(null));
    }

    @Test
    void validationResultDefensivelyCopiesAndProtectsErrorsList() {
        assertThrows(NullPointerException.class, () -> new ValidationResult(null));

        List<ValidationError> mutableSource = new ArrayList<>();
        mutableSource.add(new ValidationError("field", "NotNull", "msg"));
        ValidationResult customResult = new ValidationResult(mutableSource);
        mutableSource.clear();
        assertEquals(1, customResult.getErrors().size());

        User invalidUser = new User(null, "Alice", "alice@mail.ru", 20, 5);
        ValidationResult result = Validator.validate(invalidUser);

        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertThrows(UnsupportedOperationException.class, () -> errors.add(
                new ValidationError("f", "NotNull", "msg")
        ));
    }

    @Test
    void inheritanceAndPrivateFieldsValidatedWhileStaticIgnored() {
        User userWithNullInheritedId = new User(null, "Alice", "alice@mail.ru", 25, 5);

        ValidationResult result = Validator.validate(userWithNullInheritedId);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        ValidationError error = result.getErrors().get(0);
        assertEquals("id", error.fieldName());
        assertEquals("NotNull", error.annotationName());
        assertEquals("ID should not be null", error.message());
    }

    @Test
    void multipleViolationsAcrossDifferentAndSameFieldsAreCollected() {
        class MultiConstraintModel {
            @Size(min = 5, max = 10, message = "Too short")
            @Email(message = "Bad email")
            private final String contact = "abc";

            @NotNull(message = "Title required")
            private final String title = null;
        }

        ValidationResult result = Validator.validate(new MultiConstraintModel());

        assertFalse(result.isValid());
        Set<ValidationError> actualErrors = Set.copyOf(result.getErrors());
        Set<ValidationError> expectedErrors = Set.of(
                new ValidationError("contact", "Size", "Too short"),
                new ValidationError("contact", "Email", "Bad email"),
                new ValidationError("title", "NotNull", "Title required")
        );
        assertEquals(expectedErrors, actualErrors);
    }

    @Test
    void nullValuesAreSkippedBySizeRangeAndEmail() {
        class NullableFieldsModel {
            @Size(min = 2, max = 5)
            private final String text = null;

            @Range(min = 10, max = 20)
            private final Double score = null;

            @Email
            private final String email = null;
        }

        ValidationResult result = Validator.validate(new NullableFieldsModel());
        assertTrue(result.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "abcde", "abcdefghij"})
    void sizeBoundaryAndValidValuesPass(String validName) {
        User user = new User("1", validName, "test@domain.com", 20, 5);
        assertTrue(Validator.validate(user).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "abcdefghijk"})
    void sizeOutOfBoundsValuesFail(String invalidName) {
        User user = new User("1", invalidName, "test@domain.com", 20, 5);
        ValidationResult result = Validator.validate(user);

        assertFalse(result.isValid());
        assertEquals(List.of(new ValidationError("name", "Size", "Name must be 2..10 chars")), result.getErrors());
    }

    @ParameterizedTest
    @ValueSource(ints = {18, 50, 100})
    void rangeBoundaryAndValidValuesPass(int validAge) {
        User user = new User("1", "Bob", "bob@domain.com", validAge, 0);
        assertTrue(Validator.validate(user).isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {-5, 17, 101})
    void rangeOutOfBoundsValuesFail(int invalidAge) {
        User user = new User("1", "Bob", "bob@domain.com", invalidAge, 0);
        ValidationResult result = Validator.validate(user);

        assertFalse(result.isValid());
        assertEquals(List.of(new ValidationError("age", "Range", "Age must be 18..100")), result.getErrors());
    }

    @Test
    void rangeFailsOnNaNValue() {
        class DoubleModel {
            @Range(min = 0.0, max = 100.0, message = "Must be a valid number")
            private final Double value = Double.NaN;
        }

        ValidationResult result = Validator.validate(new DoubleModel());
        assertFalse(result.isValid());
        assertEquals("Range", result.getErrors().get(0).annotationName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "a@b.c", "first.last@sub.domain.org"})
    void validEmailsPass(String validEmail) {
        User user = new User("1", "Bob", validEmail, 25, 5);
        assertTrue(Validator.validate(user).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "plainaddress", "@missinglocal.com", "user@", "user@domain", "user @domain.com", "user@domain.com "})
    void invalidEmailsFail(String invalidEmail) {
        User user = new User("1", "Bob", invalidEmail, 25, 5);
        ValidationResult result = Validator.validate(user);

        assertFalse(result.isValid());
        assertEquals(List.of(new ValidationError("email", "Email", "Invalid email")), result.getErrors());
    }

    @Test
    void unsupportedFieldTypeThrowsValidationConfigurationExceptionEvenWhenNull() {
        class BadSizeType {
            @Size(min = 1, max = 5)
            private final Integer number = null;
        }
        class BadRangeType {
            @Range(min = 1, max = 5)
            private final String text = null;
        }
        class BadEmailType {
            @Email
            private final Object obj = null;
        }

        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new BadSizeType()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new BadRangeType()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new BadEmailType()));
    }

    @Test
    void invalidAnnotationBoundsThrowValidationConfigurationException() {
        class NegativeSizeMin {
            @Size(min = -1, max = 5)
            private final String s = "abc";
        }
        class InvertedSizeBounds {
            @Size(min = 10, max = 5)
            private final String s = "abc";
        }
        class InvertedRangeBounds {
            @Range(min = 20, max = 10)
            private final int x = 15;
        }
        class NaNRangeBound {
            @Range(min = Double.NaN, max = 10)
            private final double x = 5.0;
        }
        class InfiniteRangeBound {
            @Range(min = 0, max = Double.POSITIVE_INFINITY)
            private final double x = 5.0;
        }

        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new NegativeSizeMin()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new InvertedSizeBounds()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new InvertedRangeBounds()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new NaNRangeBound()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new InfiniteRangeBound()));
    }
}