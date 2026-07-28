package com.jmhreif.genai_java_fundamentals;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class BookController {
    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpProvider;
    private final BookTools bookTools;

    public BookController(ChatClient.Builder builder, SyncMcpToolCallbackProvider provider, BookTools bookTools) {
        this.chatClient = builder
                .defaultTools(bookTools, provider)
                .build();
        this.mcpProvider = provider;
        this.bookTools = bookTools;
    }

    @GetMapping("/vector")
    public String vector(@RequestParam String question) {
        String vectorPrompt = """
            Answer this question about books using vector search: %s

            Use the vectorSearch tool to find relevant book reviews.
            """.formatted(question);

        return chatClient.prompt()
                .user(vectorPrompt)
                .call()
                .content();
    }

    @GetMapping("/vectorPlus")
    public String vectorPlus(@RequestParam String question) {
        // Return the raw small-vs-large comparison directly, without routing it
        // through the LLM (which tends to summarize instead of echo the data).
        return bookTools.vectorPlusComparison(question);
    }

    @GetMapping("/graph")
    public String graph(@RequestParam String question) {
        String graphPrompt = """
            Answer this question about books using graph-enriched search: %s

            Use the graphEnrichedSearch tool to find relevant reviews with book, author, and genre information.
            """.formatted(question);

        return chatClient.prompt()
                .user(graphPrompt)
                .call()
                .content();
    }

    @GetMapping("/debug/tools")
    public String debugTools() {
        var callbacks = mcpProvider.getToolCallbacks();
        StringBuilder sb = new StringBuilder("Available MCP Tools:\n");
        for (var callback : callbacks) {
            sb.append("- ").append(callback.getToolDefinition().name()).append("\n");
        }
        return sb.toString();
    }

    @GetMapping("/text2cypher")
    public String text2cypher(@RequestParam String question) {
        String cypherPrompt = """
            Answer this question about the book graph: %s

            Schema - node labels: Book, Author, Review, Series, Genre, Work, User.
            Relationships:
              (Author)-[:WROTE]->(Book)
              (Book)-[:CATEGORIZED_BY]->(Genre)
              (Book)-[:PART_OF]->(Series)
              (Review)-[:WRITTEN_FOR]->(Book)
              (Book)-[:SIMILAR_TO]-(Book)
              (User)-[:SHELVED]->(Book), (User)-[:READ]->(Book), (User)-[:PUBLISHED]->(Review)

            Use the read_cypher tool to run a Cypher query that references the specific node labels and
            relationships relevant to the question (for example, count only (:Book) nodes for a question
            about books - do not match all nodes), then state the final answer in a sentence.
            """.formatted(question);

        return chatClient.prompt()
                .user(cypherPrompt)
                .call()
                .content();
    }
}
