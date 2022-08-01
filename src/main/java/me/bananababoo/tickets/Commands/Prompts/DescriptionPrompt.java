package me.bananababoo.tickets.Commands.Prompts;

import me.bananababoo.tickets.Commands.TicketCommand;
import me.bananababoo.tickets.GUI.GUIManager;
import me.bananababoo.tickets.Tickets;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionPrompt extends StringPrompt {

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return "Now input short a description of the ticket";
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext c, @Nullable String input) {
        c.setSessionData("Description", input);
        c.getForWhom().sendRawMessage(c.getSessionData("Name") + " : " + c.getSessionData("Description"));
        GUIManager.chooseCategory((Player) c.getForWhom(),c.getSessionData("Name").toString(), c.getSessionData("Description").toString());
        return END_OF_CONVERSATION;
    }
}
