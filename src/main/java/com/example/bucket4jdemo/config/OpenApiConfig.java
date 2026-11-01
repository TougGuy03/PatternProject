package com.example.bucket4jdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bucket4jOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bucket4j Architecture Demo API")
                        .description("Демонстрация ограничения HTTP-запросов с помощью Bucket4j")
                        .version("1.0.0"));
    }
}
