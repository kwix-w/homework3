package hometask8;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {
    double min() default -Double.MAX_VALUE;
    double max() default Double.MAX_VALUE;
    String message() default "Value must be within the specified range";
}
