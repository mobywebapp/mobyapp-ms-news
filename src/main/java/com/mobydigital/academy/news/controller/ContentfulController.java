package com.mobydigital.academy.news.controller;

import com.mobydigital.academy.news.dto.Audience;
import com.mobydigital.academy.news.dto.NewsDto;
import com.mobydigital.academy.news.service.ContentfulService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contentful")
@AllArgsConstructor
@Tag(
        name = "News",
        description = "Operaciones para obtener el contenido de noticias de MobyApp desde Contentful, filtrado por audiencia (web o app)."
)
public class ContentfulController {

    private final ContentfulService service;

    @Operation(
            summary = "Obtener todas las noticias activas para la mobyapp",
            description = "Devuelve todas las noticias dirigidas a la audiencia de la MobyApp. "
                    + "Cada noticia se obtiene desde Contentful, se filtra por fecha de expiración, "
                    + "y se ordena por prioridad antes de almacenarse en caché para un acceso más rápido.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de noticias activas para la aplicación de la app",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = NewsDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "No hay noticias disponibles para la audiencia de la app",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno al obtener las noticias",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Error inesperado",
                                                    value = "{\"error\": \"Error al procesar la respuesta de Contentful\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "Problema de caché o conectividad",
                                                    value = "{\"error\": \"No se pudo conectar a Contentful o a la caché Redis\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/news/app")
    public ResponseEntity<List<NewsDto>> getNewsApp() {
        List<NewsDto> news = service.buildFinalNews(Audience.MOBY_APP);
        return (news == null || news.isEmpty())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(news);
    }

    @Operation(
            summary = "Obtener todas las noticias activas para la plataforma web",
            description = "Devuelve todas las noticias dirigidas a la audiencia de Moby Web. "
                    + "Este endpoint está destinado a las aplicaciones frontend web que consumen contenido de Contentful.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de noticias activas para la plataforma web",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = NewsDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "No hay noticias disponibles para la audiencia web",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno al obtener las noticias",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Error inesperado",
                                                    value = "{\"error\": \"Error al procesar la respuesta de Contentful\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "Problema de caché o conectividad",
                                                    value = "{\"error\": \"No se pudo conectar a Contentful o a la caché Redis\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/news/web")
    public ResponseEntity<List<NewsDto>> getNewsWeb() {
        List<NewsDto> news = service.buildFinalNews(Audience.MOBY_WEB);
        return (news == null || news.isEmpty())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(news);
    }
}
