package com.duoc.bancoxyzbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BancoXyzBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoXyzBatchApplication.class, args);
	}

}
