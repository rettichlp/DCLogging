package de.rettichlp.dclogging.message;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;

public class MessageTemplate {

    private final String DEFAULT_MESSAGE_TEMPLATE = """
            <t:%timestamp%:F> **%messageTemplateType_displayName%**
            ```%messageTemplateType_codeBlock%
            %messageTemplateType_messagePrefix%%message%
            ```
            """;

    private final String messageTemplateString;

    public MessageTemplate(MessageTemplateType type) {
        this.messageTemplateString = DEFAULT_MESSAGE_TEMPLATE
                .replace("%messageTemplateType_displayName%", type.getDisplayName())
                .replace("%messageTemplateType_codeBlock%", type.getCodeBlock())
                .replace("%messageTemplateType_messagePrefix%", type.getMessagePrefix());
    }

    public MessageTemplate(String messageTemplateString) {
        this.messageTemplateString = messageTemplateString;
    }

    public String applyMessage(String message) {
        return this.messageTemplateString
                .replace("%timestamp%", valueOf(currentTimeMillis()).substring(0, 10))
                .replace("%message%", message);
    }

    public enum MessageTemplateType {

        INFO("INFORMATION", "fix", ""),
        WARN("WARNING", "bash", ""),
        ERROR("ERROR", "diff", "- ");

        /**
         * The display name of the message type.
         */
        private final String displayName;

        /**
         * The code block language of the message type to have different colors.
         * <ul>
         *     <li>fix = blue</li>
         *     <li>bash = yellow/orange</li>
         *     <li>diff = green or red (depends on the message prefix)</li>
         * </ul>
         */
        private final String codeBlock;

        /**
         * The prefix of the message used to manipulate message for different colors. Example for <code>diff</code>:
         * <ul>
         *     <li>+ = green</li>
         *     <li>- = red</li>
         * </ul>
         */
        private final String messagePrefix;

        MessageTemplateType(String displayName, String codeBlock, String messagePrefix) {
            this.displayName = displayName;
            this.codeBlock = codeBlock;
            this.messagePrefix = messagePrefix;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getCodeBlock() {
            return this.codeBlock;
        }

        public String getMessagePrefix() {
            return this.messagePrefix;
        }
    }
}
