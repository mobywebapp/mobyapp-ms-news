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
@Schema(description = "Represents a news item retrieved from Contentful.")
public class NewsDto {

    @Schema(
            description = "Unique identifier of the news entry in Contentful.",
            example = "7LkA3pQmJ2w9zAB1eQeD9P",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String id;

    @Schema(
            description = "Title of the news article.",
            example = "New Learning Path Released: Spring Boot Advanced",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Schema(
            description = "Indicates if the news item is currently active and visible.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean active;

    @Schema(
            description = "URL of the image associated with the news item.",
            example = "https://cdn.contentful.com/assets/news/header-image.jpg",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String imageUrl;

    @Schema(
            description = "Detailed description or summary of the news article.",
            example = "Discover our new Spring Boot learning path designed for experienced developers.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @Schema(
            description = "Indicates if the news item should be displayed on the Moby Web platform.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isMobyWeb;

    @Schema(
            description = "Indicates if the news item should be displayed in the Moby mobile application.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isMobyApp;

    @Schema(
            description = "External URL or link related to the news item (optional).",
            example = "https://academy.moby.com/news/spring-boot-path",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String url;

    @Schema(
            description = "Date and time when the news item expires (and should no longer be displayed).",
            example = "2025-12-31T23:59:59Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ZonedDateTime expirationDate;

    @Schema(
            description = "Date and time when the news item was originally created.",
            example = "2025-10-15T09:30:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ZonedDateTime createdAt;

    @Schema(
            description = "Indicates if the news item has high priority and should appear before others.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean priority;
}
