package com.cooperative.voting.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("agendas")
public class Agenda {
    @Id private String id;
    private String title;
    private String description;
    private Instant createdAt;
    protected Agenda() { }
    public Agenda(String title, String description, Instant createdAt) { this.title = title; this.description = description; this.createdAt = createdAt; }
    public String getId() { return id; } public String getTitle() { return title; } public String getDescription() { return description; } public Instant getCreatedAt() { return createdAt; }
}
