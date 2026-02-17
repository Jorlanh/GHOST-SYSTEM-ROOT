package com.ghost.ghost_discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class GhostDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(GhostDiscoveryApplication.class, args);
	}

}
