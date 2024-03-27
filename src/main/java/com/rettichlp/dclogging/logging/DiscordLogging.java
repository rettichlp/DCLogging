package com.rettichlp.dclogging.logging;

import com.rettichlp.dclogging.message.MessageTemplate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static com.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static com.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.INFO;
import static com.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.WARN;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.logging.LogManager.getLogManager;
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

    public void log(@NotNull String message, @NotNull Level level) {
        log(message, level, null, this.textChannelId);
    }

    public void log(@NotNull String message, @NotNull Level level, @Nullable Throwable throwable) {
        log(message, level, throwable, this.textChannelId);
    }

    public void log(@NotNull String message, @NotNull Level level, @NotNull String textChannelId) {
        log(message, level, null, textChannelId);
    }

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
            File stacktraceFile = createStacktraceFile(throwable);
            messageCreateAction.addFiles(fromData(stacktraceFile));
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

    File createStacktraceFile(Throwable throwable) {
        File stacktraceFile = new File("stacktrace_" + currentTimeMillis() + ".txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(stacktraceFile))) {
            throwable.printStackTrace(writer);
            return stacktraceFile;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
