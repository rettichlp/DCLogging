package com.rettichlp.dclogging.logging;

import com.rettichlp.dclogging.message.MessageTemplate;
import lombok.Builder;
import lombok.Data;
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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.MEMBER_OVERRIDES;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE;

@Data
@Builder
public class DiscordLogging {

    public static JDA BOT;

    private final String botToken;
    private final String guildId;
    private final String textChannelId;
    @Builder.Default
    private final boolean appendStacktraceToError = true;
    @Builder.Default
    private final MessageTemplate infoMessageTemplate = new MessageTemplate(INFO);
    @Builder.Default
    private final MessageTemplate warnMessageTemplate = new MessageTemplate(WARN);
    @Builder.Default
    private final MessageTemplate errorMessageTemplate = new MessageTemplate(ERROR);

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
        MessageCreateAction messageCreateAction = getTextChannel(textChannelId)
                .sendMessage(this.errorMessageTemplate.applyMessage(message));

        if (this.appendStacktraceToError && nonNull(throwable)) {
            File stacktraceFile = createStacktraceFile(throwable);
            messageCreateAction.addFiles(fromData(stacktraceFile));
        }

        messageCreateAction.queue();
    }

    @NotNull
    private Guild getGuild() {
        if (isNull(BOT)) {
            BOT = JDABuilder
                    .createDefault(this.botToken)
                    .disableCache(MEMBER_OVERRIDES, VOICE_STATE)
                    .build();
        }

        return ofNullable(BOT.getGuildById(this.guildId))
                .orElseThrow(() -> new NullPointerException("Bot is not a member in guild with id " + this.guildId));
    }

    private void send(@NotNull String textChannelId, @NotNull String message) {
        getTextChannel(textChannelId).sendMessage(message).queue();
    }

    @NotNull
    TextChannel getTextChannel(@NotNull String textChannelId) {
        Guild guild = getGuild();
        return ofNullable(guild.getTextChannelById(textChannelId))
                .orElseThrow(() -> new NullPointerException("TextChannel not found in guild " + guild.getName() + " (" + this.guildId + ")"));
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
