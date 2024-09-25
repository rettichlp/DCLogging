package de.rettichlp.dclogging.logging;

import de.rettichlp.dclogging.message.MessageTemplate;
import lombok.Builder;
import lombok.Getter;
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

import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.INFO;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.WARN;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;
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

    public void log(@NotNull String message, @NotNull Level level, @Nullable Throwable throwable, @NotNull String textChannelId) {
        switch (level) {
            case INFO -> info(message, textChannelId);
            case WARN -> warn(message, textChannelId);
            case ERROR -> error(message, throwable, textChannelId);
        }
    }

    public void info(@NotNull String message) {
        info(message, this.textChannelId);
    }

    public void info(@NotNull String message, @NotNull String textChannelId) {
        send(textChannelId, this.infoMessageTemplate.applyMessage(message));
    }

    public void warn(@NotNull String message) {
        warn(message, this.textChannelId);
    }

    public void warn(@NotNull String message, @NotNull String textChannelId) {
        send(textChannelId, this.warnMessageTemplate.applyMessage(message));
    }

    public void error(@NotNull String message) {
        error(message, null, this.textChannelId);
    }

    public void error(@NotNull String message, @Nullable Throwable throwable) {
        error(message, throwable, this.textChannelId);
    }

    public void error(@NotNull String message, @Nullable Throwable throwable, @NotNull String textChannelId) {
        TextChannel textChannel = textChannelId.isBlank() ? getGuild().getSystemChannel() : getTextChannel(textChannelId);
        MessageCreateAction messageCreateAction = ofNullable(textChannel)
                .orElseThrow(() -> new IllegalStateException("No textChannelId specified and no System-Channel found in guild with id '" + this.guildId + "'"))
                .sendMessage(this.errorMessageTemplate.applyMessage(message));

        if (this.appendStacktraceToError && nonNull(throwable)) {
            messageCreateAction.addFiles(fromData(throwableToInputStream(throwable), "stacktrace.txt"));
        }

        messageCreateAction.queue();
    }

    @NotNull
    private Guild getGuild() {
        return ofNullable(this.jda.getGuildById(this.guildId))
                .orElseThrow(() -> new IllegalStateException("Bot is not a member in guild with id '" + this.guildId + "'"));
    }

    private void send(@NotNull String textChannelId, @NotNull String message) {
        TextChannel textChannel = textChannelId.isBlank() ? getGuild().getSystemChannel() : getTextChannel(textChannelId);

        ofNullable(textChannel)
                .map(tc -> tc.sendMessage(message))
                .orElseThrow(() -> new IllegalStateException("No textChannelId specified and no System-Channel found in guild with id '" + this.guildId + "'"))
                .queue();
    }

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
    TextChannel getTextChannel(@NotNull String textChannelId) {
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
