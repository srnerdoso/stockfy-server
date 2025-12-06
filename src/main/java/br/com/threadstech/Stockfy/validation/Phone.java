package br.com.threadstech.stockfy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {

  String message() default "Número de telefone inválido.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String region() default "BR";
}
