package com.senai.gestao_beneficios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestaoBeneficiosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestaoBeneficiosApplication.class, args);
	}

}
