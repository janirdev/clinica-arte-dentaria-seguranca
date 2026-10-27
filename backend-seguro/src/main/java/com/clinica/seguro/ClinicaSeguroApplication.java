package com.clinica.seguro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Versao CORRIGIDA da API Clinica Arte Dentaria (porta 8081).
 * Serve para comparar, na aula, com a versao vulneravel (porta 8080).
 */
@SpringBootApplication
public class ClinicaSeguroApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClinicaSeguroApplication.class, args);
    }
}
