package de.rettichlp.dclogging.logging;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.INFO;
import static org.slf4j.event.Level.WARN;

public class DiscordLoggingTest {

    private TextChannel textChannel;

    private MessageCreateAction messageCreateAction;

    private DiscordLogging discordLogging;

    @BeforeEach
    public void setUp() {
        this.textChannel = mock(TextChannel.class);
        this.messageCreateAction = mock(MessageCreateAction.class);
        this.discordLogging = spy(DiscordLogging.getBuilder()
                .botToken("botToken")
                .guildId("guildId")
                .textChannelId("textChannelId")
                .build());
    }

    @Test
    public void testDiscordLoggingInfo() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_info";
        this.discordLogging.info(message);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingWarn() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_warn";
        this.discordLogging.warn(message);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingError() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_error";
        this.discordLogging.error(message);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingLevelInfo() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_info";
        this.discordLogging.log(message, INFO);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingLevelWarn() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_warn";
        this.discordLogging.log(message, WARN);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingLevelError() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_error";
        this.discordLogging.log(message, ERROR);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingDifferentTextChannelInfo() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        String differentTextChannelId = "differentTextChannelId";
        doReturn(this.textChannel).when(this.discordLogging).getTextChannel(differentTextChannelId);

        String message = "message_info";
        this.discordLogging.info(message, differentTextChannelId);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingDifferentTextChannelWarn() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        String differentTextChannelId = "differentTextChannelId";
        doReturn(this.textChannel).when(this.discordLogging).getTextChannel(differentTextChannelId);

        String message = "message_warn";
        this.discordLogging.warn(message, differentTextChannelId);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingDifferentTextChannelError() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        String differentTextChannelId = "differentTextChannelId";
        doReturn(this.textChannel).when(this.discordLogging).getTextChannel(differentTextChannelId);

        String message = "message_error";
        this.discordLogging.error(message, null, differentTextChannelId);

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingStacktrace() {
        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);
        when(this.messageCreateAction.addFiles(any(FileUpload.class))).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(this.discordLogging).getTextChannel("textChannelId");

        String message = "message_error";
        this.discordLogging.error(message, new NullPointerException("test exception"));

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - %s
                ```
                """.formatted(message))));
        verify(this.discordLogging).throwableToInputStream(any(Throwable.class));
        verify(this.messageCreateAction).addFiles(any(FileUpload.class));
        verify(this.messageCreateAction).queue();
    }

    @Test
    public void testDiscordLoggingStacktraceDisallowed() {
        DiscordLogging discordLogging = spy(DiscordLogging.getBuilder()
                .botToken("botToken")
                .guildId("guildId")
                .textChannelId("textChannelId")
                .appendStacktraceToError(false)
                .build());

        when(this.textChannel.sendMessage(anyString())).thenReturn(this.messageCreateAction);
        when(this.messageCreateAction.addFiles(any(FileUpload.class))).thenReturn(this.messageCreateAction);

        doReturn(this.textChannel).when(discordLogging).getTextChannel("textChannelId");

        String message = "message_error";
        discordLogging.error(message, new NullPointerException("test exception"));

        verify(this.textChannel).sendMessage(argThat(new RegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - %s
                ```
                """.formatted(message))));
        verify(this.messageCreateAction).queue();
        verifyNoMoreInteractions(this.messageCreateAction);
    }

    private static class RegexArgumentMatcher implements ArgumentMatcher<String> {

        private final Pattern pattern;

        RegexArgumentMatcher(String regex) {
            this.pattern = compile(regex);
        }

        @Override
        public boolean matches(String argument) {
            return this.pattern.matcher(argument).matches();
        }
    }
}
