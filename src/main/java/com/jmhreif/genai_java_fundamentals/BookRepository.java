package com.jmhreif.genai_java_fundamentals;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface BookRepository extends Neo4jRepository<Book, String> {
    @Query("MATCH (b:Book)<-[rel:WRITTEN_FOR]-(r:Review) " +
            "WHERE r.id IN $reviewIds " +
            "RETURN b, collect(rel), collect(r);")
    List<Book> findBooks(List<String> reviewIds);

    // book_id + title + (first) author, returned in the same order as the input review ids.
    // Aliased columns (not a node projection) so the computed author maps into the DTO.
    @Query("UNWIND $reviewIds AS rid " +
            "MATCH (r:Review {id: rid})-[:WRITTEN_FOR]->(b:Book) " +
            "RETURN b.book_id AS bookId, b.title AS title, head([(a:Author)-[:WROTE]->(b) | a.name]) AS author;")
    List<BookHit> hitsForReviews(List<String> reviewIds);
}
