package com.bpts.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentTransferServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local development server");

        Server dockerServer = new Server();
        dockerServer.setUrl("http://localhost:8080");
        dockerServer.setDescription("Docker container server");

        Contact contact = new Contact();
        contact.setName("Payment Transfer Service Support");
        contact.setEmail("support@bpts.com");
        contact.setUrl("https://github.com/your-repo/bpts");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Payment Transfer Service API")
                .version("1.0.0")
                .description("RESTful API for secure payment transfers between accounts. " +
                           "This service enables users to transfer funds between accounts within " +
                           "the same banking platform with comprehensive validation and error handling.")
                .termsOfService("https://github.com/your-repo/bpts/terms")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, dockerServer));
    }
}