package br.com.threadstech.stockfy;

import br.com.threadstech.stockfy.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Test
@WithMockUser(username = "admin", roles = "ADMIN")
public @interface AdminTest {}
