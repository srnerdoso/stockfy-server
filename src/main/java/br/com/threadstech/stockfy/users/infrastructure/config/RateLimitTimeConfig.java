package br.com.threadstech.stockfy.users.infrastructure.config;

import io.github.bucket4j.TimeMeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitTimeConfig {

	@Bean
	public TimeMeter rateLimitTimeMeter() {
		return TimeMeter.SYSTEM_NANOTIME;
	}

}
