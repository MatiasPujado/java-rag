package dev.danvega.markets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    @GetMapping("/")
    public String chat() {
        return chatClient.prompt()
                .system("You are an assistant that answers questions based on the provided context. Use ONLY the information from the context to answer the question. If the context doesn't contain relevant information, say that you don't have enough information to answer.")
                .user("How did the Federal Reserve's recent interest rate cut impact various asset classes according to the analysis")
                .call()
                .content();
    }
}
