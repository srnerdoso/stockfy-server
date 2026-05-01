package br.com.threadstech.stockfy.modules.users.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

class RabbitMqConfigTest {

  @Test
  @DisplayName("Deve configurar conversor JSON para eventos RabbitMQ")
  void jsonMessageConverter_whenCreated_thenUsesJacksonJsonConverter() {
    var converter = new RabbitMqConfig().jsonMessageConverter();

    assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
  }
}
