package com.example.controller

import com.example.dto.DummyResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller
class DummyController {

    @Post("/dummy")
    fun respond(): DummyResponse {
        return DummyResponse("", "second", null)
    }
}