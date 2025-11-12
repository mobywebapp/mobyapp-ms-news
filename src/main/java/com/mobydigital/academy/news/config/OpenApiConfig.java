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
                        .title("News moby API")
                        .description("""
                            Provides access to MobyApp's news content retrieved from Contentful.
                            This service filters, sorts, and caches active news by audience type (web/app),
                            and performs scheduled cleanup tasks to remove expired entries automatically.
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Moby Digital – News with Contentful")
                                .email("talento@mobydigital.com")
                                .url("https://www.mobydigital.com/hablemos"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Swagger/OpenAPI Reference")
                        .url("https://swagger.io/docs/"))
                .addTagsItem(new Tag()
                        .name("News")
                        .description("Operations for retrieving and maintaining MobyApp news content from Contentful, "
                                + "including audience filtering, caching, and cleanup of expired entries."));
    }
}
