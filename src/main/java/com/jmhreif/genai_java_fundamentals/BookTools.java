package com.jmhreif.genai_java_fundamentals;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookTools {
    private final Neo4jVectorStore vectorStore;
    private final BookRepository repo;

    public BookTools(Neo4jVectorStore vectorStore, BookRepository repo) {
        this.vectorStore = vectorStore;
        this.repo = repo;
    }

    @Tool(description = "Search for book reviews similar to the query using vector similarity")
    public String vectorSearch(String query) {
        List<Document> results = vectorStore.similaritySearch(query);

        String formattedResults = results.stream()
                .map(Document::toString)
                .collect(Collectors.joining("\n"));
        System.out.println("----- Vector Search Tool Results -----");
        System.out.println(formattedResults);

        return formattedResults;
    }

    @Tool(description = "Get books (with their reviews) enriched from the graph for the query")
    public String graphEnrichedSearch(String query) {
        List<Document> vectorResults = vectorStore.similaritySearch(query);

        List<Book> books = repo.findBooks(
                vectorResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        String formattedResults = books.stream()
                .map(Book::toString)
                .collect(Collectors.joining("\n"));
        System.out.println("----- Graph Enriched Search Tool Results -----");
        System.out.println(formattedResults);

        return formattedResults;
    }

    @Tool(description = "Show how vector search relevance decays as the result set grows: a short vs a longer list of matched books")
    public String vectorPlusComparison(String query) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(20).build());

        // Books behind the matched reviews, in vector-rank order (UNWIND preserves input order).
        List<String> books = repo.hitsForReviews(results.stream().map(Document::getId).toList()).stream()
                .map(hit -> hit.title() + " by " + (hit.author() != null ? hit.author() : "Unknown author"))
                .toList();

        String output = """
                SHORT LIST (top 5):
                %s

                LONGER LIST (top %d):
                %s
                """.formatted(
                        String.join("\n", books.subList(0, Math.min(5, books.size()))),
                        books.size(),
                        String.join("\n", books));

        System.out.println(output);
        return output;
    }
}
