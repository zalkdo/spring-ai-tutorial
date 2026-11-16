package com.example.spring_ai_tutorial.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.support.HttpRequestWrapper
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestClient
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets


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

                val response = execution.execute(request, body)
                val responseBytes = StreamUtils.copyToByteArray(response.body)
                var responseBody = String(responseBytes, StandardCharsets.UTF_8)


                // 응답 JSON에 usage 필드가 없으면 기본값을 추가
                if (responseBody.contains("\"data\"") && !responseBody.contains("\"usage\"")) {
                    responseBody = responseBody.replaceFirst(
                        Regex("\\}\\s*$"),
                        ""","usage":{"prompt_tokens":0,"total_tokens":0}}"""
                    )
                }

                val modifiedBytes = responseBody.toByteArray(StandardCharsets.UTF_8)
                object : ClientHttpResponse by response {
                    override fun getBody(): InputStream = ByteArrayInputStream(modifiedBytes)

                    override fun getHeaders(): HttpHeaders {
                        return HttpHeaders().apply {
                            putAll(response.headers)
                            contentLength = modifiedBytes.size.toLong()
                        }
                    }
                }
            }


        return OpenAiApi.builder()
            .baseUrl(geminiUrl)
            .apiKey(geminiKey)
            .restClientBuilder(restClientBuilder)
            .build()
    }
}