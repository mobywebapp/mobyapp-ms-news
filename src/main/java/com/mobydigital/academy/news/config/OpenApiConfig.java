package com.mobydigital.academy.news.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Noticias – Moby")
                        .description("""
                                Proporciona acceso al contenido de noticias de MobyApp obtenido desde Contentful.
                                Este servicio filtra, ordena y cachea las noticias activas según la audiencia (web/app),
                                además de ejecutar tareas programadas para eliminar automáticamente las entradas expiradas.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Moby Digital – Servicio de Noticias con Contentful")
                                .email("talento@mobydigital.com")
                                .url("https://www.mobydigital.com/hablemos"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Referencia oficial de Swagger/OpenAPI")
                        .url("https://swagger.io/docs/"))
                .addTagsItem(new Tag()
                        .name("News")
                        .description("""
                                Operaciones para obtener y gestionar el contenido de noticias de MobyApp desde Contentful,
                                incluyendo filtrado por audiencia, manejo de caché y limpieza automática de noticias expiradas.
                                """));
    }
}
