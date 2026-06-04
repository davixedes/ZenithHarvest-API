package com.fiap.zenith.analise_svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AnaliseSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnaliseSvcApplication.class, args);
	}

}
