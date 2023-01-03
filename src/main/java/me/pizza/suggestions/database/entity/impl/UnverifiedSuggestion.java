package me.pizza.suggestions.database.entity.impl;

import lombok.Builder;
import lombok.Data;
import me.pizza.suggestions.database.entity.Suggestion;

@Data
@Builder
public class UnverifiedSuggestion implements Suggestion {

    private final int suggestionId;
    private final long messageId;

    private final long authorId;
    private final String content;

    @Override
    public long suggestionId() {
        return this.suggestionId;
    }
    @Override
    public long messageId() {
        return this.messageId;
    }

    @Override
    public long authorId() {
        return this.authorId;
    }

    @Override
    public String content() {
        return this.content;
    }
}
