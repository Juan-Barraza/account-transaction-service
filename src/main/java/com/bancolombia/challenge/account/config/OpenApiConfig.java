package com.bancolombia.challenge.account.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountTransactionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Transaction Service API")
                        .description("API to manage accounts and transactions")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Account transaction service")
                                .email("juanbarraza01718@gmail.com")
                        )
                );

    }
}
