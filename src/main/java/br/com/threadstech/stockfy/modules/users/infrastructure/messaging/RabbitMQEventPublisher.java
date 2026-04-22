package br.com.threadstech.stockfy.modules.users.infrastructure.messaging;

import br.com.threadstech.stockfy.modules.users.domain.event.AccountLockedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  private static final String EXCHANGE = "security.exchange";
  private static final String ROUTING_KEY = "account.locked";

  public void publish(AccountLockedEvent event) {
    rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
  }
}
