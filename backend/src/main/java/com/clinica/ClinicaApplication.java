package com.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LABORATORIO DE SEGURANCA - Clinica Arte Dentaria.
 *
 * ATENCAO: esta aplicacao e PROPOSITADAMENTE INSEGURA e serve apenas para
 * ensino de vulnerabilidades em ambiente de laboratorio isolado (localhost).
 * NAO colocar em producao nem expor a internet. NAO usar dados reais.
 */
@SpringBootApplication
public class ClinicaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }
}
