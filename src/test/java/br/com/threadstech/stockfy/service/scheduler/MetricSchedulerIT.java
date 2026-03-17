package br.com.threadstech.stockfy.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.threadstech.stockfy.annotations.IntegrationTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@IntegrationTests
class MetricSchedulerIT {

  @Autowired private ApplicationContext context;

  @Test
  @DisplayName("Should load cron expression from application properties")
  void shouldLoadCronExpressionFromApplicationProperties() {
    MetricScheduler scheduler = context.getBean(MetricScheduler.class);
    Method method = ReflectionUtils.findMethod(MetricScheduler.class, "processMetrics");

    assertThat(method).isNotNull();
    Scheduled scheduled = method.getAnnotation(Scheduled.class);
    assertThat(scheduled).isNotNull();

    String cronExpression = scheduled.cron();
    assertThat(cronExpression).isEqualTo("${scheduler.metric.cron}");

    String resolvedCron = context.getEnvironment().resolvePlaceholders(cronExpression);
    assertThat(resolvedCron).isEqualTo("0 0 0 * * *");
  }
}
