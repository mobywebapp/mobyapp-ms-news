package com.mobydigital.academy.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Audience",
        description = "Target audience for news delivery."
)
public enum Audience {
    MOBY_APP, MOBY_WEB
}