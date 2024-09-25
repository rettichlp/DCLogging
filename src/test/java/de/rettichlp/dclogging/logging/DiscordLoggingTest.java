package de.rettichlp.dclogging.logging;

import de.rettichlp.dclogging.exception.InvalidChannelIdException;
import de.rettichlp.dclogging.exception.InvalidGuildIdException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiscordLoggingTest {

    private JDA jdaMock;
    private Guild guildMock;
    private TextChannel textChannelMock;
    private MessageCreateAction messageCreateAction;
    private DiscordLogging discordLogging;

    @BeforeEach
    void setUp() {
        this.jdaMock = mock(JDA.class);
        this.guildMock = mock(Guild.class);
        this.textChannelMock = mock(TextChannel.class);
        this.messageCreateAction = mock(MessageCreateAction.class);

        // Configure mocks
        when(this.jdaMock.getGuildById(anyString())).thenReturn(this.guildMock);
        when(this.guildMock.getTextChannelById(anyString())).thenReturn(this.textChannelMock);
        when(this.textChannelMock.sendMessage(anyString())).thenReturn(this.messageCreateAction);

        // Create the DiscordLogging instance
        this.discordLogging = DiscordLogging.builder()
                .jda(this.jdaMock)
                .guildId("testGuildId")
                .textChannelId("testChannelId")
                .build();
    }

    @Test
    void testInfoMessageSend() {
        this.discordLogging.info("Test information message");

        StringRegexArgumentMatcher stringRegexArgumentMatcher = new StringRegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                Test information message
                ```
                """);

        // Verify that the message was sent
        verify(this.textChannelMock, times(1)).sendMessage(argThat(stringRegexArgumentMatcher));
        verify(this.messageCreateAction, times(1)).queue();
    }

    @Test
    void testWarnMessageSend() {
        this.discordLogging.warn("Test warning message");

        StringRegexArgumentMatcher stringRegexArgumentMatcher = new StringRegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                Test warning message
                ```
                """);

        // Verify that the message was sent
        verify(this.textChannelMock, times(1)).sendMessage(argThat(stringRegexArgumentMatcher));
        verify(this.messageCreateAction, times(1)).queue();
    }

    @Test
    void testErrorMessageSend() {
        this.discordLogging.error("Test error message");

        StringRegexArgumentMatcher stringRegexArgumentMatcher = new StringRegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - Test error message
                ```
                """);

        // Verify that the message was sent
        verify(this.textChannelMock, times(1)).sendMessage(argThat(stringRegexArgumentMatcher));
        verify(this.messageCreateAction, times(1)).queue();
    }

    @Test
    void testInfoMessageSendWithArguments() {
        this.discordLogging.info("Test {} {}", "information", "message");

        StringRegexArgumentMatcher stringRegexArgumentMatcher = new StringRegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                Test information message
                ```
                """);

        // Verify that the message was sent
        verify(this.textChannelMock, times(1)).sendMessage(argThat(stringRegexArgumentMatcher));
        verify(this.messageCreateAction, times(1)).queue();
    }

    @Test
    void testWarnMessageSendWithArguments() {
        this.discordLogging.warn("Test {} {}", "warning", "message");

        StringRegexArgumentMatcher stringRegexArgumentMatcher = new StringRegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                Test warning message
                ```
                """);

        // Verify that the message was sent
        verify(this.textChannelMock, times(1)).sendMessage(argThat(stringRegexArgumentMatcher));
        verify(this.messageCreateAction, times(1)).queue();
    }

    @Test
    void testErrorMessageSendWithArguments() {
        this.discordLogging.error("Test {} {}", "error", "message");

        StringRegexArgumentMatcher stringRegexArgumentMatcher = new StringRegexArgumentMatcher("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - Test error message
                ```
                """);

        // Verify that the message was sent
        verify(this.textChannelMock, times(1)).sendMessage(argThat(stringRegexArgumentMatcher));
        verify(this.messageCreateAction, times(1)).queue();
    }

    @Test
    void testInvalidGuildIdThrowsException() {
        // Simulate the scenario where the guild is not found
        when(this.jdaMock.getGuildById(anyString())).thenReturn(null);

        assertThrows(InvalidGuildIdException.class, () -> discordLogging.info("Test message"));
    }

    @Test
    void testInvalidChannelIdThrowsException() {
        // Simulate the scenario where the text channel is not found
        when(this.guildMock.getTextChannelById(anyString())).thenReturn(null);

        assertThrows(InvalidChannelIdException.class, () -> discordLogging.info("Test message"));
    }

    private static class StringRegexArgumentMatcher implements ArgumentMatcher<String> {

        private final Pattern pattern;

        StringRegexArgumentMatcher(String regex) {
            this.pattern = compile(regex);
        }

        @Override
        public boolean matches(String argument) {
            return this.pattern.matcher(argument).matches();
        }
    }
}
