package br.com.threadstech.stockfy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.threadstech.stockfy.PostgreTestContainer;
import br.com.threadstech.stockfy.StockfyApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = StockfyApplication.class)
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ImportAutoConfiguration
@Import(PostgreTestContainer.class)
public @interface IntegrationTests {}
