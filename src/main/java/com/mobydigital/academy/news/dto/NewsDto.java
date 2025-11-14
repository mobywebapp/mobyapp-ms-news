package com.mobydigital.academy.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Representa una noticia obtenida desde Contentful.")
public class NewsDto {

    @Schema(
            description = "Identificador único de la noticia dentro de Contentful.",
            example = "7LkA3pQmJ2w9zAB1eQeD9P",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String id;

    @Schema(
            description = "Título de la noticia.",
            example = "Nuevo Learning Path disponible: Spring Boot Avanzado",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String title;

    @Schema(
            description = "Indica si la noticia está actualmente activa y visible.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean active;

    @Schema(
            description = "URL de la imagen asociada a la noticia.",
            example = "https://cdn.contentful.com/assets/news/header-image.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String imageUrl;

    @Schema(
            description = "Descripción detallada o resumen del contenido de la noticia.",
            example = "Descubrí nuestro nuevo Learning Path de Spring Boot diseñado para desarrolladores con experiencia.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    @Schema(
            description = "Indica si la noticia debe mostrarse en la plataforma web de Moby.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isMobyWeb;

    @Schema(
            description = "Indica si la noticia debe mostrarse en la aplicación móvil de Moby.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isMobyApp;

    @Schema(
            description = "Enlace o URL externa relacionada con la noticia (opcional).",
            example = "https://academy.moby.com/news/spring-boot-path",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String url;

    @Schema(
            description = "Fecha y hora en la que la noticia expira y deja de mostrarse.",
            example = "2025-12-31T23:59:59Z",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private ZonedDateTime expirationDate;

    @Schema(
            description = "Fecha y hora en la que la noticia fue creada originalmente.",
            example = "2025-10-15T09:30:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ZonedDateTime createdAt;

    @Schema(
            description = "Indica si la noticia tiene prioridad alta y debe mostrarse antes que otras.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean priority;
}
