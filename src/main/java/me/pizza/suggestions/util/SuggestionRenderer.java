package me.pizza.suggestions.util;

import com.google.common.collect.ImmutableMap;
import jdk.nashorn.internal.objects.annotations.Getter;
import me.pizza.suggestions.config.BotConfig;
import me.pizza.suggestions.database.entity.Suggestion;
import me.pizza.suggestions.database.entity.SuggestionType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

public class SuggestionRenderer {

    private static final Map<SuggestionType, String> FOOTER_MAP = new EnumMap<>(SuggestionType.class);

    static {
        FOOTER_MAP.put(SuggestionType.ACCEPTED, "Accepted by %s");
        FOOTER_MAP.put(SuggestionType.DECLINED, "Declined by %s");
        FOOTER_MAP.put(SuggestionType.IMPLEMENTED, "Implemented by %s");
    }

    public static MessageEmbed createRenderEmbed(Suggestion suggestion, Member member, Color color) {
        StringBuilder footer = new StringBuilder("Suggestion ID: " + suggestion.suggestionId());

        final MessageEmbed unverifiedEmbed = new EmbedBuilder()
                .setTitle("A wild suggestion appeared")
                .setDescription("A new suggestion has been submitted by "
                        + member.getAsMention() + "!\n\n```" + suggestion.content() + "```")
                .setFooter("Suggestion ID: " + suggestion.suggestionId())
                .setTimestamp(Instant.now())
                .setColor(color)
                .build();

        return unverifiedEmbed;
    }

    public static MessageEmbed createRenderEmbed(Suggestion suggestion, User author, Member member,
                                                 SuggestionType suggestionType, Color color) {
        String footerFormat = FOOTER_MAP.getOrDefault(suggestionType, "Suggestion Id: " + suggestion.suggestionId());
        String footer = String.format("Suggestion ID: " + suggestion.suggestionId() + " | %s", String.format(footerFormat, author.getName()));

        final MessageEmbed changedEmbed = new EmbedBuilder()
                .setTitle("A wild suggestion appeared")
                .setDescription("A new suggestion has been submitted by "
                        + member.getAsMention() + "!\n\n```" + suggestion.content() + "```")
                .setFooter(footer)
                .setTimestamp(Instant.now())
                .setColor(color)
                .build();

        return changedEmbed;
    }

    public static MessageEmbed createSuggestionsEmbed() {
        final MessageEmbed createdSuggestionEmbed = new EmbedBuilder()
                .setTitle("Suggestion sent successfully!")
                .setDescription("Your suggestion has been successfully sent to the server team and will soon be " +
                        "analyzed and answered between **accepted**, **denied** and **implemented**.")
                .setColor(Color.YELLOW)
                .build();

        return createdSuggestionEmbed;
    }
}
