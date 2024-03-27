package com.rettichlp.dclogging.message;

import org.junit.jupiter.api.Test;

import static com.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.ERROR;
import static com.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.INFO;
import static com.rettichlp.dclogging.message.MessageTemplate.MessageTemplateType.WARN;

public class MessageTemplateTest {

    @Test
    public void testMessageTemplateInfo() {
        MessageTemplate messageTemplate = new MessageTemplate(INFO);

        String message = "test_message_info";
        String result = messageTemplate.applyMessage(message);

        assert result.matches("""
                <t:\\d{10}:F> \\*\\*INFORMATION\\*\\*
                ```fix
                %s
                ```
                """.formatted(message));
    }

    @Test
    public void testMessageTemplateWarn() {
        MessageTemplate messageTemplate = new MessageTemplate(WARN);

        String message = "test_message_warn";
        String result = messageTemplate.applyMessage(message);

        assert result.matches("""
                <t:\\d{10}:F> \\*\\*WARNING\\*\\*
                ```bash
                %s
                ```
                """.formatted(message));
    }

    @Test
    public void testMessageTemplateError() {
        MessageTemplate messageTemplate = new MessageTemplate(ERROR);

        String message = "test_message_error";
        String result = messageTemplate.applyMessage(message);

        assert result.matches("""
                <t:\\d{10}:F> \\*\\*ERROR\\*\\*
                ```diff
                - %s
                ```
                """.formatted(message));
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
