package de.rettichlp.dclogging.logging;

import de.rettichlp.dclogging.message.MessageTemplate;
import lombok.Builder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;

@Builder
public class LogMessage {

    private static final String ARGUMENT_PLACEHOLDER = "\\{}";

    private final String message;
    @Builder.Default
    private final Object[] arguments = new Object[0];
    private final MessageTemplate.MessageTemplateType messageTemplateType;
    private final Throwable throwable;

    public void send(@NotNull TextChannel textChannel) {
        // populate the message with the arguments
        String populatedMessage = Stream.of(this.arguments)
                .reduce(this.message, (result, arg) -> result.replaceFirst(ARGUMENT_PLACEHOLDER, arg.toString()), (s1, s2) -> s1);

        // apply message to message template
        String formattedMessage = new MessageTemplate(this.messageTemplateType).applyMessage(populatedMessage);

        // create message
        MessageCreateAction messageCreateAction = textChannel.sendMessage(formattedMessage);

        // add stacktrace if throwable is not null
        if (nonNull(this.throwable)) {
            messageCreateAction.addFiles(fromData(throwableToInputStream(this.throwable), "stacktrace.txt"));
        }

        // send message
        messageCreateAction.queue();
    }

    InputStream throwableToInputStream(@NotNull Throwable throwable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        throwable.printStackTrace(ps);
        ps.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
