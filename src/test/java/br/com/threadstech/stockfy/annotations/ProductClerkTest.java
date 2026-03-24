package br.com.threadstech.stockfy.annotations;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Test
@WithMockUser(username = "clerk", roles = "PRODUCT_CLERK")
public @interface ProductClerkTest {}
