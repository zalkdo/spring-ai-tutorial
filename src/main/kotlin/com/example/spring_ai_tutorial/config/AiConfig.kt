package com.example.spring_ai_tutorial.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.ai.google.genai.api.GoogleGenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
public class AiConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${spring.ai.openai.api-key}")
    private lateinit var openaiKey: String

    @Value("\${spring.ai.google.genai.api-key}")
    private lateinit var geminiKey: String


    /**
     * OpenAI API 클라이언트 빈 등록
     */
    @Bean
    fun openAiApi(): OpenAiApi {
        logger.debug { "OpenAI API 클라이언트 초기화" }
        return OpenAiApi.builder()
            .apiKey(openaiKey)
            .build()
    }


    fun googleGeminiApi(): GoogleGenAiApi {
        logger.debug { "Google GenAI (Gemini) API 클라이언트 초기화" }
        return GoogleGenAiApi.builder()
            .apiKey(geminiKey)
            .build()
    }
}