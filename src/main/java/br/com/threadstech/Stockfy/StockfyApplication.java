package br.com.threadstech.stockfy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockfyApplication {

  public static void main(String[] args) {
    SpringApplication.run(StockfyApplication.class, args);
  }
}
