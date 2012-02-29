package validation;

import net.sf.oval.configuration.annotation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
 
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = CCNumberLengthCheck.class)
public @interface CCNumber {
	String message() default CCNumberLengthCheck.message;
	int value();
}
