package org.java_avanzado.taller.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the REST API documentation.
 *
 * <p>Configures API metadata including title, version, description, contact information,
 * and JWT bearer token security scheme for API documentation and testing via Swagger UI.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates a customized OpenAPI specification with JWT authentication.
     *
     * @return the OpenAPI specification with metadata and security schemes
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API REST TALLER FINAL")
                        .version("1.0.0")
                        .description("REST API for e-commerce platform managing users, products, orders, and audit logs.")
                        .contact(new Contact()
                                .name("API Support"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/license/mit")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}