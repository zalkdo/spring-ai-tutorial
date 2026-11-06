package com.example.spring_ai_tutorial.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.support.HttpRequestWrapper
import org.springframework.web.client.RestClient
import java.net.URI

/**
 * OpenAI API 설정
 */
@Configuration
class OpenAiConfig {
    private val logger = KotlinLogging.logger {}

    @Value("\${spring.ai.openai.api-key}")
    private lateinit var apiKey: String

    @Value("\${spring.ai.google.genai.api-key}")
    private lateinit var geminiKey: String

    @Value("\${spring.ai.google.genai.base-url}")
    private lateinit var geminiUrl: String

    /**
     * OpenAI API 클라이언트 빈 등록
     */
    @Bean
    fun openAiApi(): OpenAiApi {
        logger.debug { "OpenAI API 클라이언트 초기화" }
        return OpenAiApi.builder()
            .apiKey(apiKey)
            .build()
    }

    /**
     * Google Gemini API 클라이언트 빈 등록
     */
    @Bean
    fun googleGeminiApi(): OpenAiApi {
        logger.debug { "Google Gemini 클라이언트 초기화" }

        var restClientBuilder = RestClient.builder()
            .requestInterceptor { request, body, execution ->
                val uriString = request.uri.toString()
                logger.info { "Google AI Studio Original URL : $uriString" }
                if(uriString.contains("/openai/v1/")) {
                    val fixedUri = URI.create(uriString.replace("/openai/v1/", "/openai/"))
                    var modifiedRequest = object : HttpRequestWrapper(request) {
                        override fun getURI(): URI = fixedUri
                    }
                    logger.info { "Google AI Studio Fixed URL : ${request.uri.toString()}" }
                    execution.execute(modifiedRequest, body)
                }else{
                    execution.execute(request, body)
                }
            }


        return OpenAiApi.builder()
            .baseUrl(geminiUrl)
            .apiKey(geminiKey)
            .restClientBuilder(restClientBuilder)
            .build()
    }
}