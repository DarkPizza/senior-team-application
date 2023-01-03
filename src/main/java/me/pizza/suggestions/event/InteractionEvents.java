package me.pizza.suggestions.event;

import me.pizza.suggestions.config.BotConfig;
import me.pizza.suggestions.database.entity.Suggestion;
import me.pizza.suggestions.database.entity.SuggestionType;
import me.pizza.suggestions.database.entity.impl.UnverifiedSuggestion;
import me.pizza.suggestions.service.SuggestionService;
import me.pizza.suggestions.util.SuggestionRenderer;
import me.pizza.suggestions.util.SuggestionUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Optional;

public class InteractionEvents extends ListenerAdapter {

    private final BotConfig botConfig;

    private final SuggestionService suggestionService;

    private final Logger logger;

    public InteractionEvents(BotConfig botConfig, SuggestionService suggestionService) {
        this.botConfig = botConfig;
        this.suggestionService = suggestionService;

        this.logger = LoggerFactory.getLogger(InteractionEvents.class);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        final String subCmdName = event.getSubcommandName();

        String suggestionChannel = this.botConfig.getSuggestionChannel();

        switch (subCmdName) {
            case "create":
                final TextInput suggestionInput = TextInput
                        .create("suggestion-input", "Suggestions", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Write your suggestion here")
                        .setMinLength(10)
                        .setMaxLength(100)
                        .build();

                final Modal suggestionModal = Modal
                        .create("suggestion-modal", "Suggestion")
                        .addActionRows(ActionRow.of(suggestionInput))
                        .build();

                event.replyModal(suggestionModal).queue();
                break;

            case "accept":
                this.handleSuggestion(event, suggestionChannel, "accept");
                break;

            case "decline":
                this.handleSuggestion(event, suggestionChannel, "decline");
                break;
            case "implement":
                this.handleSuggestion(event, suggestionChannel, "implement");
                break;

        }
        super.onSlashCommandInteraction(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        final String modalInputId = "suggestion-input";
        final String modalInteractionId = "suggestion-modal";

        final Member suggestionCreator = event.getMember();

        if (event.getInteraction().getModalId().equals(modalInteractionId)) {
            event.replyEmbeds(SuggestionRenderer.createSuggestionsEmbed()).setEphemeral(true).queue();

            final String suggestionContent = event.getValue(modalInputId).getAsString();
            final String suggestionChannel = this.botConfig.getSuggestionChannel();

            final Guild botGuild = event.getJDA().getGuildById(this.botConfig.getGuild());
            final TextChannel botChannel = botGuild.getTextChannelById(suggestionChannel);

            final UnverifiedSuggestion.UnverifiedSuggestionBuilder suggestionBuilder = UnverifiedSuggestion
                    .builder();

            suggestionBuilder.authorId(suggestionCreator.getIdLong());
            suggestionBuilder.content(suggestionContent);

            final Message message = botChannel
                    .sendMessageEmbeds(SuggestionRenderer.createRenderEmbed(suggestionBuilder.build(),
                            suggestionCreator, Color.GRAY)).complete();

            suggestionBuilder.messageId(message.getIdLong());
            this.suggestionService.createSuggestion(suggestionBuilder.build());

            final UnverifiedSuggestion insertedSuggestion = this.suggestionService.getSuggestionMapper()
                    .getUnverifiedSuggestionByMessageId(message.getIdLong());

            message.editMessageEmbeds(SuggestionRenderer.createRenderEmbed(insertedSuggestion,
                    suggestionCreator, Color.GRAY)).queue();
        }
        super.onModalInteraction(event);
    }

    public void handleSuggestion(SlashCommandInteractionEvent event, String suggestionChannel, String subCmd) {
        final TextChannel channel = event.getGuild().getTextChannelById(suggestionChannel);

        final Color commandColor = SuggestionUtil.getColorForStatus(subCmd);
        final SuggestionType suggestionType = SuggestionUtil.getCategoryForStatus(subCmd);

        final int suggestionId = event.getOption("suggestion-id").getAsInt();

        final User commandAuthor = event.getMember().getUser();

        final Optional<Suggestion> suggestion = this.suggestionService
                .getSuggestionByType(suggestionId, SuggestionType.UNVERIFIED);

        if (!suggestion.isPresent()) {
            event.reply("Not that one... I looked everywhere for the ID of this suggestion and couldn't find " +
                    "it, make sure everything is correct.").setEphemeral(true).queue();
            return;
        }

        final UnverifiedSuggestion foundSuggestion = (UnverifiedSuggestion) suggestion.get();

        final Optional<Message> suggestionMessage = channel.retrieveMessageById(foundSuggestion.messageId())
                .map(Optional::ofNullable).complete();

        if (!suggestionMessage.isPresent()) {
            event.reply("Not that one... I looked everywhere for the suggestion message and couldn't find it, " +
                    "make sure everything is correct.").setEphemeral(true).queue();
            return;
        }

        final Message foundMessage = suggestionMessage.get();

        this.suggestionService.moveSuggestionByCategory(suggestionId, suggestionType, event.getMember().getIdLong());
        final Member suggestionCreator = event.getGuild().getMemberById(foundSuggestion.getAuthorId());

        foundMessage.editMessageEmbeds(SuggestionRenderer.createRenderEmbed(foundSuggestion, commandAuthor,
                suggestionCreator, suggestionType, commandColor)).queue();
        event.reply("You have successfully changed the category of the suggestion to: `" + suggestionType.name() + "`")
                .setEphemeral(true).queue();
    }
}