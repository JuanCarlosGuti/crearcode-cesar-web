package com.crearcode.leads;

import org.springframework.boot.SpringApplication;

public class TestLeadsApplication {

	public static void main(String[] args) {
		SpringApplication.from(LeadsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
