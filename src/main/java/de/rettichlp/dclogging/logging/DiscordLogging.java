package de.rettichlp.dclogging.logging;

import de.rettichlp.dclogging.message.MessageTemplate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.INFO;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.WARN;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.MEMBER_OVERRIDES;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE;

public class DiscordLogging {

    private final JDA jda;
    private final String guildId;
    private final String textChannelId;
    private final boolean appendStacktraceToError;
    private final MessageTemplate infoMessageTemplate;
    private final MessageTemplate warnMessageTemplate;
    private final MessageTemplate errorMessageTemplate;

    public DiscordLogging(JDA jda, String guildId, String textChannelId, boolean appendStacktraceToError, MessageTemplate infoMessageTemplate, MessageTemplate warnMessageTemplate, MessageTemplate errorMessageTemplate) {
        this.jda = jda;
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.appendStacktraceToError = appendStacktraceToError;
        this.infoMessageTemplate = infoMessageTemplate;
        this.warnMessageTemplate = warnMessageTemplate;
        this.errorMessageTemplate = errorMessageTemplate;
    }

    /**
     * Logs a message at the specified level.
     *
     * @param message the message to log
     * @param level   the level at which to log the message
     */
    public void log(@NotNull String message, @NotNull Level level) {
        log(message, level, null, this.textChannelId);
    }

    /**
     * Logs a message and an optional throwable at the specified level.
     *
     * @param message   the message to log
     * @param level     the level at which to log the message
     * @param throwable an optional throwable to log
     */
    public void log(@NotNull String message, @NotNull Level level, @Nullable Throwable throwable) {
        log(message, level, throwable, this.textChannelId);
    }

    /**
     * Logs a message at the specified level to a specific text channel.
     *
     * @param message       the message to log
     * @param level         the level at which to log the message
     * @param textChannelId the ID of the text channel to log the message to
     */
    public void log(@NotNull String message, @NotNull Level level, @NotNull String textChannelId) {
        log(message, level, null, textChannelId);
    }

    /**
     * Logs a message and an optional throwable at the specified level to a specific text channel.
     *
     * @param message       the message to log
     * @param level         the level at which to log the message
     * @param throwable     an optional throwable to log
     * @param textChannelId the ID of the text channel to log the message to
     *
     * @throws IllegalStateException if the text channel cannot be found
     */
    public void log(@NotNull String message, @NotNull Level level, @Nullable Throwable throwable, @NotNull String textChannelId) throws IllegalStateException {
        switch (level) {
            case INFO -> info(message, textChannelId);
            case WARN -> warn(message, textChannelId);
            case ERROR -> error(message, throwable, textChannelId);
        }
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     * @param args    optional arguments to format the message
     */
    public void info(@NotNull String message, Object... args) {
        info(populate(message, args), this.textChannelId);
    }

    /**
     * Logs an informational message to a specific text channel.
     *
     * @param message       the message to log
     * @param textChannelId the ID of the text channel to log the message to
     */
    public void info(@NotNull String message, @NotNull String textChannelId) {
        send(textChannelId, this.infoMessageTemplate.applyMessage(message));
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     * @param args    optional arguments to format the message
     */
    public void warn(@NotNull String message, Object... args) {
        warn(populate(message, args), this.textChannelId);
    }

    /**
     * Logs a warning message to a specific text channel.
     *
     * @param message       the message to log
     * @param textChannelId the ID of the text channel to log the message to
     */
    public void warn(@NotNull String message, @NotNull String textChannelId) {
        send(textChannelId, this.warnMessageTemplate.applyMessage(message));
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     * @param args    optional arguments to format the message
     *
     * @throws IllegalStateException if the text channel cannot be found
     */
    public void error(@NotNull String message, Object... args) throws IllegalStateException {
        error(populate(message, args), null, this.textChannelId);
    }

    /**
     * Logs an error message and an optional throwable.
     *
     * @param message   the message to log
     * @param throwable an optional throwable to log
     *
     * @throws IllegalStateException if the text channel cannot be found
     */
    public void error(@NotNull String message, @Nullable Throwable throwable) throws IllegalStateException {
        error(message, throwable, this.textChannelId);
    }

    /**
     * Logs an error message and an optional throwable to a specific text channel.
     *
     * @param message       the message to log
     * @param throwable     an optional throwable to log
     * @param textChannelId the ID of the text channel to log the message to
     *
     * @throws IllegalStateException if the text channel cannot be found
     */
    public void error(@NotNull String message, @Nullable Throwable throwable, @NotNull String textChannelId) throws IllegalStateException {
        TextChannel textChannel = textChannelId.isBlank() ? getGuild().getSystemChannel() : getTextChannel(textChannelId);
        MessageCreateAction messageCreateAction = ofNullable(textChannel)
                .orElseThrow(() -> new IllegalStateException("No textChannelId specified and no System-Channel found in guild with id '" + this.guildId + "'"))
                .sendMessage(this.errorMessageTemplate.applyMessage(message));

        if (this.appendStacktraceToError && nonNull(throwable)) {
            messageCreateAction.addFiles(fromData(throwableToInputStream(throwable), "stacktrace.txt"));
        }

        messageCreateAction.queue();
    }

    /**
     * Retrieves the guild associated with the bot.
     *
     * @return the Guild object
     *
     * @throws IllegalStateException if the bot is not a member of the guild
     */
    @NotNull
    private Guild getGuild() throws IllegalStateException {
        return ofNullable(this.jda.getGuildById(this.guildId))
                .orElseThrow(() -> new IllegalStateException("Bot is not a member in guild with id '" + this.guildId + "'"));
    }

    /**
     * Sends a message to a specified text channel.
     *
     * @param textChannelId the ID of the text channel to send the message to
     * @param message       the message to send
     *
     * @throws IllegalStateException if the text channel cannot be found
     */
    private void send(@NotNull String textChannelId, @NotNull String message) throws IllegalStateException {
        TextChannel textChannel = textChannelId.isBlank() ? getGuild().getSystemChannel() : getTextChannel(textChannelId);

        ofNullable(textChannel)
                .map(tc -> tc.sendMessage(message))
                .orElseThrow(() -> new IllegalStateException("No textChannelId specified and no System-Channel found in guild with id '" + this.guildId + "'"))
                .queue();
    }

    /**
     * Populates a template string with the provided arguments.
     *
     * @param template the template string
     * @param args     the arguments to populate the template with
     *
     * @return the populated string
     */
    private String populate(String template, Object[] args) {
        return Stream.of(args)
                .reduce(template, (result, arg) -> result.replaceFirst("\\{}", arg.toString()), (s1, s2) -> s1);
    }

    /**
     * Returns a new instance of the Builder class.
     *
     * @return a new Builder instance
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String botToken = "";
        private String guildId = "";
        private String textChannelId = "";
        private boolean appendStacktraceToError = true;
        private MessageTemplate infoMessageTemplate = new MessageTemplate(INFO);
        private MessageTemplate warnMessageTemplate = new MessageTemplate(WARN);
        private MessageTemplate errorMessageTemplate = new MessageTemplate(ERROR);

        public Builder botToken(String botToken) {
            this.botToken = botToken;
            return this;
        }

        public Builder guildId(String guildId) {
            this.guildId = guildId;
            return this;
        }

        public Builder textChannelId(String textChannelId) {
            this.textChannelId = textChannelId;
            return this;
        }

        public Builder appendStacktraceToError(boolean appendStacktraceToError) {
            this.appendStacktraceToError = appendStacktraceToError;
            return this;
        }

        public Builder infoMessageTemplate(MessageTemplate infoMessageTemplate) {
            this.infoMessageTemplate = infoMessageTemplate;
            return this;
        }

        public Builder warnMessageTemplate(MessageTemplate warnMessageTemplate) {
            this.warnMessageTemplate = warnMessageTemplate;
            return this;
        }

        public Builder errorMessageTemplate(MessageTemplate errorMessageTemplate) {
            this.errorMessageTemplate = errorMessageTemplate;
            return this;
        }

        public DiscordLogging build() {
            if (this.botToken.isBlank()) {
                throw new IllegalStateException("Bot-Token is not set");
            }

            JDA jda = this.botToken.equals("botToken") ? null : JDABuilder
                    .createDefault(this.botToken)
                    .disableCache(MEMBER_OVERRIDES, VOICE_STATE)
                    .build();

            return new DiscordLogging(
                    jda,
                    this.guildId,
                    this.textChannelId,
                    this.appendStacktraceToError,
                    this.infoMessageTemplate,
                    this.warnMessageTemplate,
                    this.errorMessageTemplate);
        }
    }

    @NotNull
    TextChannel getTextChannel(@NotNull String textChannelId) throws IllegalStateException {
        Guild guild = getGuild();
        return ofNullable(guild.getTextChannelById(textChannelId))
                .orElseThrow(() -> new IllegalArgumentException("TextChannel not found in guild " + guild.getName() + " (" + this.guildId + ")"));
    }

    InputStream throwableToInputStream(Throwable throwable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        throwable.printStackTrace(ps);
        ps.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
