package me.bananababoo.tickets.Commands.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NamePrompt extends StringPrompt {

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {

        return "Enter Ticket Name";
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext c, @Nullable String input) {
        c.setSessionData("Name", input);

        return new DescriptionPrompt();
    }
}
