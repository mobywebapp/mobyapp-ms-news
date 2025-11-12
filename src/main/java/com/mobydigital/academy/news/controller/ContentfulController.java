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
        description = "Operations for retrieving MobyApp news content fetched from Contentful, filtered by audience (web or app)."
)
public class ContentfulController {

    private final ContentfulService service;

    @Operation(
            summary = "Get all active news for the mobile app",
            description = "Returns all news items targeted to the MobyApp audience. "
                    + "Each news entry is fetched from Contentful, filtered by expiration date, "
                    + "and sorted by priority before being cached for faster access.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of active news for the mobile app",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = NewsDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "No news available for the web audience",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error during news retrieval",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Unexpected error",
                                                    value = "{\"error\": \"Error processing Contentful response\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "Cache or connectivity issue",
                                                    value = "{\"error\": \"Unable to connect to Contentful or Redis cache\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/news/app")
    public ResponseEntity<List<NewsDto>> getNewsApp() {
        List<NewsDto> news = service.buildFinalNews(Audience.MOBY_APP);
        return news == null || news.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(news);
    }

    @Operation(
            summary = "Get all active news for the web platform",
            description = "Returns all news items targeted to the Moby Web audience. "
                    + "This endpoint is intended for frontend web applications that display Contentful content.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of active news for the web platform",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = NewsDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "No news available for the web audience",
                            content = @Content(
                            mediaType = "application/json",
                                    schema = @Schema(example = "")
                    )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error during news retrieval",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Unexpected error",
                                                    value = "{\"error\": \"Error processing Contentful response\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "Cache or connectivity issue",
                                                    value = "{\"error\": \"Unable to connect to Contentful or Redis cache\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/news/web")
    public ResponseEntity<List<NewsDto>> getNewsWeb() {
        List<NewsDto> news = service.buildFinalNews(Audience.MOBY_WEB);
        return news == null || news.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(news);
    }
}
