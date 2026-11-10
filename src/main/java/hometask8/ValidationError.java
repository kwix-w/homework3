package hometask8;

public record ValidationError(String fieldName, String annotationName, String message) {

    public String getFieldName() {
        return fieldName;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public String getMessage() {
        return message;
    }
}