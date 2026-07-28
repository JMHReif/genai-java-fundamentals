package com.jmhreif.genai_java_fundamentals;

import org.springframework.data.neo4j.core.schema.Id;

public record Review(@Id String id,
                     String text,
                     Integer rating) {
}
