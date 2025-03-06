package com.github.bytebandits.bithub;

import java.util.UUID;

public class MoodPost {
    private UUID id;
    private String content;

    public MoodPost(UUID id, String content) {
        this.id = id;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
