package me.pizza.suggestions.database.entity;

import me.pizza.suggestions.database.entity.impl.DeclinedSuggestion;
import me.pizza.suggestions.database.entity.impl.UnverifiedSuggestion;

public interface Suggestion {

    long suggestionId();

    long messageId();

    long authorId();

    String content();

}
