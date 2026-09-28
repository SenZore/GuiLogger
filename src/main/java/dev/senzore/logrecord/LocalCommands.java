package dev.senzore.logrecord;

import java.util.Locale;

public final class LocalCommands {
    private LocalCommands() {}

    public static boolean isRecorder(String input) {
        String command = normalize(input);
        return command.equals("logrecord") || command.startsWith("logrecord ")
                || command.startsWith("logrecord\t") || command.startsWith("logrecord\n");
    }

    public static boolean isRecorderSuggestion(String input) {
        String command = normalize(input);
        return isRecorder(command) || (command.startsWith("logr") && "logrecord".startsWith(command));
    }

    private static String normalize(String input) {
        String command = input.stripLeading();
        if (command.startsWith("/")) command = command.substring(1);
        return command.toLowerCase(Locale.ROOT);
    }
}
