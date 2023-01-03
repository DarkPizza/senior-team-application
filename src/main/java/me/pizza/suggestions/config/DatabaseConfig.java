package me.pizza.suggestions.config;

import org.skife.config.Config;

public interface DatabaseConfig {

    @Config("database.url")
    String getUrl();

    @Config("database.user")
    String getUser();

    @Config("database.password")
    String getPass();
}
