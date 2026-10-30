package com.example.spring_ai_tutorial.config

import com.google.genai.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.google.genai.GoogleGenAiChatModel
import org.springframework.ai.google.genai.GoogleGenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration


@Configuration
public class AiConfig {

    private val logger = KotlinLogging.logger {}


    @Value("\${spring.ai.google.genai.api-key}")
    private lateinit var geminiKey: String


    fun googleGeminiApi(): GoogleGenAiChatOptions {
        logger.debug { "Google GenAI (Gemini) API 클라이언트 초기화" }
        return GoogleGenAiChatOptions.builder().model(GoogleGenAiChatModel.ChatModel.GEMINI_2_0_FLASH).temperature(0.4).build()
    }

    var genAiClient: Client = Client.builder()
        .apiKey(geminiKey) //System.getenv("GOOGLE_API_KEY"))
        .build()

    var chatModel: GoogleGenAiChatModel = GoogleGenAiChatModel.builder()
        .genAiClient(genAiClient)
        .defaultOptions(
            GoogleGenAiChatOptions.builder().model(GoogleGenAiChatModel.ChatModel.GEMINI_2_0_FLASH).temperature(0.4).build()
        ).build()


//    var response: ChatResponse = chatModel.call(
//        Prompt("Generate the names of 5 famous pirates.")
//    )
}