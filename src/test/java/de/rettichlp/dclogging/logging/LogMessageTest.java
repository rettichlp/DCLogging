package de.rettichlp.dclogging.logging;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogMessageTest {

    private TextChannel textChannelMock;
    private MessageCreateAction messageCreateActionMock;

    @BeforeEach
    void setUp() {
        this.textChannelMock = mock(TextChannel.class);
        this.messageCreateActionMock = mock(MessageCreateAction.class);

        // Configure mocks
        when(this.textChannelMock.sendMessage(anyString())).thenReturn(this.messageCreateActionMock);
    }

    @Test
    void testSendMessageWithoutThrowable() {
        LogMessage logMessage = LogMessage.builder()
                .message("Test message without throwable")
                .messageTemplateType(ERROR)
                .build();

        logMessage.send(this.textChannelMock);

        // Verify that the message was sent without files
        verify(this.textChannelMock, times(1)).sendMessage(anyString());
        verify(this.messageCreateActionMock, times(0)).addFiles(any(FileUpload.class));
    }

    @Test
    void testSendMessageWithThrowable() {
        Throwable throwable = new RuntimeException("Test Exception");

        LogMessage logMessage = LogMessage.builder()
                .message("Test message with throwable")
                .messageTemplateType(ERROR)
                .throwable(throwable)
                .build();

        logMessage.send(this.textChannelMock);

        // Verify that the message was sent with a file (stack trace)
        verify(this.textChannelMock, times(1)).sendMessage(anyString());
        verify(this.messageCreateActionMock, times(1)).addFiles(any(FileUpload.class));
    }
}
