package me.bananababoo.tickets.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import me.bananababoo.tickets.Commands.Prompts.NamePrompt;
import me.bananababoo.tickets.GUI.GUIManager;
import me.bananababoo.tickets.Tickets;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@CommandAlias("ticket|tk")
public class TicketCommand extends BaseCommand implements Listener {

    Component chatInput;


    @Default
    public void onDefault(Player player) {
        Bukkit.getLogger().info("test");
        GUIManager.openGUI(player);
    }

    @Subcommand("create")
    public static void create(Player p) {
        ConversationFactory convo = Tickets.getConversation().withModality(true);
        Conversation c = convo.withFirstPrompt(new NamePrompt()).buildConversation(p);
        c.begin();
    }


}




