package me.pizza.suggestions.config;

import org.skife.config.Config;

public interface BotConfig {
    @Config("bot.token")
    String getToken();

    @Config("guild.id")
    String getGuild();

    @Config("bot.commands")
    boolean shouldCreateCommands();

    @Config("guild.channel.suggestions")
    String getSuggestionChannel();
}