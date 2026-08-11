package com.crearcode.leads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
// Los datos de la empresa para el PDF de la cotización (F11) llegan como
// @ConfigurationProperties; el escaneo los registra sin declararlos uno a uno.
@ConfigurationPropertiesScan
public class LeadsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeadsApplication.class, args);
	}

}
