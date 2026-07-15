package br.com.threadstech.stockfy;

import org.springframework.boot.SpringApplication;

public class TestStockfyApplication {

	public static void main(String[] args) {
		SpringApplication.from(StockfyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
