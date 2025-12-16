package br.com.threadstech.stockfy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Test
@WithMockUser(username = "inventory manager", roles = "INVENTORY_MANAGER")
public @interface InventoryManagerTest {}
