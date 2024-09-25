package de.rettichlp.dclogging.logging;

import de.rettichlp.dclogging.exception.InvalidChannelIdException;
import de.rettichlp.dclogging.exception.InvalidGuildIdException;
import de.rettichlp.dclogging.message.MessageTemplate;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.INFO;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.WARN;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.MEMBER_OVERRIDES;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE;

@Builder
public class DiscordLogging {

    /**
     * The Java Discord API (JDA) instance that handles the interaction with the Discord API. This instance is used to send messages,
     * join guilds, and manage other Discord-related operations. It is initialized through the {@code botToken()} method in the
     * builder.
     */
    @Getter
    private final JDA jda;

    /**
     * The ID of the Discord guild (server) where logging messages will be sent. This field defaults to an empty string if no guild ID
     * is provided.
     */
    @Builder.Default
    private final String guildId = "";

    /**
     * The ID of the Discord text channel where logging messages will be sent. This field defaults to an empty string if no text
     * channel ID is provided.
     */
    @Builder.Default
    private final String textChannelId = "";

    /**
     * Indicates whether stack traces should be appended to error messages. This field is set to {@code true} by default, meaning that
     * stack traces will be included in error messages unless specified otherwise.
     */
    @Builder.Default
    private final boolean appendStacktraceToError = true;

    /**
     * The template used for sending informational messages (e.g., logs at the INFO level). This field defaults to a standard
     * {@link MessageTemplate} for INFO-level messages.
     */
    @Builder.Default
    private final MessageTemplate infoMessageTemplate = new MessageTemplate(INFO);

    /**
     * The template used for sending warning messages (e.g., logs at the WARN level). This field defaults to a standard
     * {@link MessageTemplate} for WARN-level messages.
     */
    @Builder.Default
    private final MessageTemplate warnMessageTemplate = new MessageTemplate(WARN);

    /**
     * The template used for sending error messages (e.g., logs at the ERROR level). This field defaults to a standard
     * {@link MessageTemplate} for ERROR-level messages.
     */
    @Builder.Default
    private final MessageTemplate errorMessageTemplate = new MessageTemplate(ERROR);

    /**
     * Logs an informational message to the specified text channel. The message can contain placeholders for additional arguments,
     * which will be passed in the {@code args} parameter. This method constructs a log message of type {@code INFO} and sends it to
     * the specified or default text channel.
     *
     * @param message the message template to log; must not be null
     * @param args    additional arguments to fill in the placeholders of the message
     *
     * @throws InvalidChannelIdException if the text channel ID is invalid or no channel is found
     */
    public void info(@NotNull String message, Object... args) {
        LogMessage logMessage = LogMessage.builder()
                .message(message)
                .arguments(args)
                .messageTemplateType(INFO)
                .build();

        logMessage.send(getTextChannel(this.textChannelId));
    }

    /**
     * Logs a warning message to the specified text channel. The message can contain placeholders for additional arguments, which will
     * be passed in the {@code args} parameter. This method constructs a log message of type {@code WARN} and sends it to the specified
     * or default text channel.
     *
     * @param message the message template to log; must not be null
     * @param args    additional arguments to fill in the placeholders of the message
     *
     * @throws InvalidChannelIdException if the text channel ID is invalid or no channel is found
     */
    public void warn(@NotNull String message, Object... args) {
        LogMessage logMessage = LogMessage.builder()
                .message(message)
                .arguments(args)
                .messageTemplateType(WARN)
                .build();

        logMessage.send(getTextChannel(this.textChannelId));
    }

    /**
     * Logs an error message to the specified text channel. This method acts as a shortcut for logging error messages without
     * specifying a throwable. The message can contain placeholders for additional arguments, which will be passed in the {@code args}
     * parameter.
     *
     * @param message the message template to log; must not be null
     * @param args    additional arguments to fill in the placeholders of the message
     *
     * @throws InvalidChannelIdException if the text channel ID is invalid or no channel is found
     */
    public void error(@NotNull String message, Object... args) {
        error(message, null, args);
    }

    /**
     * Logs an error message with an optional throwable to the specified text channel. The message can contain placeholders for
     * additional arguments, which will be passed in the {@code args} parameter. If a throwable is provided, it will be included in the
     * log message.
     *
     * @param message   the message template to log; must not be null
     * @param throwable the throwable to log (optional); may be null
     * @param args      additional arguments to fill in the placeholders of the message
     *
     * @throws InvalidChannelIdException if the text channel ID is invalid or no channel is found
     */
    public void error(@NotNull String message, @Nullable Throwable throwable, Object... args) {
        LogMessage logMessage = LogMessage.builder()
                .message(message)
                .arguments(args)
                .messageTemplateType(ERROR)
                .throwable(throwable)
                .build();

        logMessage.send(getTextChannel(this.textChannelId));
    }

    /**
     * Retrieves the text channel to which messages should be sent. If the {@code textChannelId} is null, the system channel of the
     * guild is used. If no system channel or matching text channel is found, an {@code InvalidChannelIdException} is thrown.
     *
     * @param textChannelId the ID of the text channel (optional); may be null
     *
     * @return the {@code TextChannel} object to which messages should be sent
     *
     * @throws InvalidChannelIdException if no valid channel is found
     */
    @NotNull
    private TextChannel getTextChannel(@Nullable String textChannelId) {
        Guild guild = getGuild();

        TextChannel textChannel = isNull(textChannelId)
                ? guild.getSystemChannel()
                : guild.getTextChannelById(textChannelId);

        return ofNullable(textChannel)
                .orElseThrow(() -> new InvalidChannelIdException("No textChannelId specified and no System-Channel found or no TextChannel found with id " + textChannelId + " in guild " + guild.getName() + " (" + this.guildId + ")"));
    }

    /**
     * Retrieves the guild associated with the bot using the guild ID. If no guild is found or the bot is not a member of the guild, an
     * {@code InvalidGuildIdException} is thrown.
     *
     * @return the {@code Guild} object representing the bot's guild
     *
     * @throws InvalidGuildIdException if the guild ID is invalid or the bot is not a member
     */
    @NotNull
    private Guild getGuild() {
        return ofNullable(this.jda.getGuildById(this.guildId))
                .orElseThrow(() -> new InvalidGuildIdException("Bot is not a member in guild with id '" + this.guildId + "'"));
    }

    /**
     * A builder class for configuring and initializing Discord logging functionality. This builder allows you to set up the necessary
     * configurations such as the Discord bot token.
     * <p>
     * Example usage:
     * <pre>{@code
     * DiscordLoggingBuilder builder = new DiscordLoggingBuilder()
     *      .botToken("your-bot-token");
     * }</pre>
     */
    public static class DiscordLoggingBuilder {

        /**
         * Sets the Discord bot token and initializes the JDA (Java Discord API) instance. This method configures the bot with the
         * provided token, disables certain caches, and waits for the bot to be ready.
         *
         * @param botToken the Discord bot token. This token must not be blank.
         *
         * @return the {@code DiscordLoggingBuilder} instance for chaining additional configuration.
         *
         * @throws InterruptedException if the thread is interrupted while waiting for the JDA to be ready.
         */
        public DiscordLoggingBuilder botToken(String botToken) throws InterruptedException {
            this.jda = botToken.equals("botToken") ? null : JDABuilder
                    .createDefault(botToken)
                    .disableCache(MEMBER_OVERRIDES, VOICE_STATE)
                    .build().awaitReady();

            return this;
        }
    }
}
