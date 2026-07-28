package com.jmhreif.genai_java_fundamentals;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm")
public class AIController {
    private final ChatClient chatClient;

    public AIController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    //Provide prompt to LLM for book recommendations
    @GetMapping()
    public String generateLLMResponse(@RequestParam(defaultValue = "Who is Jennifer Reif?") String question) {

        return chatClient.prompt().user(question).call().content();
    }

    //Provide prompt to LLM for book recommendations
    @GetMapping("/books")
    public String generateBookRecommendations(@RequestParam String question) {

        return chatClient.prompt().user(question).call().content();
    }
}
