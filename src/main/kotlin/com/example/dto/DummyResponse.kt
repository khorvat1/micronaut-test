package com.example.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DummyResponse(
        val first: String?,
        val second: String?,
        val third: String?
)