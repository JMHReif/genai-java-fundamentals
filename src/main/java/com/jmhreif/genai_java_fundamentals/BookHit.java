package com.jmhreif.genai_java_fundamentals;

// DTO projection for the vector-decay comparison: a book's id, title, and (first) author.
// Plain record (not @Node) so SDN maps the query's projected keys - including the
// computed author - straight to the constructor. Field is bookId, not book_id:
// SDN parses an underscore as a nested property path.
public record BookHit(String bookId,
                      String title,
                      String author) {
}
