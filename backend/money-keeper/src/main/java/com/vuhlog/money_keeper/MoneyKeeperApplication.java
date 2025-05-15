package com.vuhlog.money_keeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class MoneyKeeperApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyKeeperApplication.class, args);
	}

}
