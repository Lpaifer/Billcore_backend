package com.billcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillcoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillcoreApplication.class, args);
	}

}
