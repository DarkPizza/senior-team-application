package me.pizza.suggestions.event;

import lombok.Getter;
import me.pizza.suggestions.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GuildEvents extends ListenerAdapter {

    @Getter
    private final BotConfig botConfig;

    public GuildEvents(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        final JDA jda = event.getJDA();
        final List<Guild> loadedGuilds = jda.getGuilds();

        final Optional<Guild> botGuild = Optional
                .ofNullable(jda.getGuildById(this.botConfig.getGuild()));

        if(!botGuild.isPresent()) {
            throw new RuntimeException("guild in config not found");
        }

        final Guild botGuildFound = botGuild.get();

        loadedGuilds.stream()
                .filter(guild -> !guild.getId().equals(botGuildFound.getId()))
                .map(Guild::leave)
                .forEach(RestAction::queue);

        if(botConfig.shouldCreateCommands()) {
            final CommandCreateAction rootCommand = botGuildFound
                    .upsertCommand("suggest", "create your suggestion");

            final SubcommandData create = new SubcommandData("create", "Will display a menu for creating a suggestion");

            final SubcommandData accept = new SubcommandData("accept", "Will mark an existing suggestion as ACCEPTED");
            final SubcommandData decline = new SubcommandData("decline", "Will mark an existing suggestion as DECLINED");
            final SubcommandData implement = new SubcommandData("implement", "Will mark an existing suggestion as IMPLEMENTED");

            accept.addOption(OptionType.INTEGER, "suggestion-id", "the suggestion id", true);
            decline.addOption(OptionType.INTEGER, "suggestion-id", "the suggestion id", true);
            implement.addOption(OptionType.INTEGER, "suggestion-id", "the suggestion id", true);

            rootCommand.addSubcommands(create, accept, implement, decline).queue();
            return;
        }

        super.onGuildReady(event);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        final String memberId = event.getMember().getId();
        final String botId = event.getJDA().getSelfUser().getId();

        final String guildId = event.getGuild().getId();
        final String botGuildId = this.botConfig.getGuild();

        if(botId.equals(memberId) && !guildId.equals(botGuildId)) {
            event.getGuild().leave().queue();
        }

        super.onGuildMemberJoin(event);
    }
}
