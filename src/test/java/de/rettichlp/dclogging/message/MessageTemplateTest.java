package de.rettichlp.dclogging.message;

import org.junit.jupiter.api.Test;

import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.INFO;
import static de.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.WARN;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTemplateTest {

    @Test
    void testApplyMessageInfo() {
        MessageTemplate template = new MessageTemplate(INFO);

        String formattedMessage = template.applyMessage("This is a test message");

        assertTrue(formattedMessage.matches("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                This is a test message
                ```
                """));
    }

    @Test
    void testApplyMessageWarn() {
        MessageTemplate template = new MessageTemplate(WARN);

        String formattedMessage = template.applyMessage("This is a test message");

        assertTrue(formattedMessage.matches("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                This is a test message
                ```
                """));
    }

    @Test
    void testApplyMessageError() {
        MessageTemplate template = new MessageTemplate(ERROR);

        String formattedMessage = template.applyMessage("This is a test message");

        assertTrue(formattedMessage.matches("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - This is a test message
                ```
                """));
    }

    @Test
    public void testMessageTemplateCustom() {
        String customMessageTemplate = "%timestamp% - %message% - test123";
        MessageTemplate messageTemplate = new MessageTemplate(customMessageTemplate);

        String message = "test_message_custom";
        String result = messageTemplate.applyMessage(message);

        assert result.matches("\\d{10} - " + message + " - test123");
    }
}
