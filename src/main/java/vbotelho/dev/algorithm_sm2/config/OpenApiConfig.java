package vbotelho.dev.algorithm_sm2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SM-2 Spaced Repetition API")
                        .description("High-performance spaced repetition API based on the SM-2 algorithm")
                        .version("1.0.0")
                        .contact(new Contact().name("vbotelho dev")));
    }
}
