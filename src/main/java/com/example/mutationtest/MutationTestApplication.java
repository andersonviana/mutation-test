package com.example.mutationtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Spring Boot desta POC.
 * A aplicação em si não expõe endpoints HTTP: o foco é demonstrar o uso
 * do PIT (Pitest) sobre serviços gerenciados pelo Spring (injeção de
 * dependência, @Service, etc.) nos 3 cenários de complexidade crescente.
 */
@SpringBootApplication
public class MutationTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MutationTestApplication.class, args);
    }
}
