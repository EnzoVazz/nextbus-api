package br.com.fiap.nextbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NextbusApplication {

	public static void main(String[] args) {
		SpringApplication.run(NextbusApplication.class, args);
	}

}
