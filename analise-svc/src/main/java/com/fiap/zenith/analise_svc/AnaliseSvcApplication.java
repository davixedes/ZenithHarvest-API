package com.fiap.zenith.analise_svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.fiap.zenith.analise_svc.infra.satellite.SentinelHubProperties;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties(SentinelHubProperties.class)
public class AnaliseSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnaliseSvcApplication.class, args);
	}

}
