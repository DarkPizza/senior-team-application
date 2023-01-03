package me.pizza.suggestions.database.entity.impl;

import lombok.Builder;
import lombok.Getter;
import me.pizza.suggestions.database.entity.Suggestion;

@Builder
public class ImplementedSuggestion implements Suggestion {

    private final long suggestionId;
    private final long messageId;

    private final long authorId;

    private final String content;

    @Getter
    private final long implementer;

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
