package br.com.threadstech.stockfy;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class RateLimitTestConfiguration {

	@Bean
	@Primary
	public MutableTimeMeter mutableRateLimitTimeMeter() {
		return new MutableTimeMeter();
	}

}
