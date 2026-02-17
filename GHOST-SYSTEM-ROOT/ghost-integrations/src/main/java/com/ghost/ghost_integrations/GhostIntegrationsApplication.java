package com.ghost.ghost_integrations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // Habilita o registro no Eureka
public class GhostIntegrationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GhostIntegrationsApplication.class, args);
	}

}
