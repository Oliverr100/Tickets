package me.bananababoo.tickets;

import co.aikar.commands.PaperCommandManager;
import me.bananababoo.tickets.Commands.TicketCommand;
import me.bananababoo.tickets.Database.MongodbServer;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.plugin.java.JavaPlugin;

public final class Tickets extends JavaPlugin {

    private static Tickets instance;
    public static ConversationFactory conversation;

    @Override
    public void onEnable() {
        instance = this;

        MongodbServer.connect();
        PaperCommandManager manager = new PaperCommandManager(Tickets.getPlugin());
        manager.registerCommand(new TicketCommand());

        conversation = new ConversationFactory(this)
                .withModality(true);

//        for (int i = 0; i < 50; i++) {
//            Bukkit.getScheduler().runTaskLater(this, () -> {
//                Ticket t = new Ticket("Bananababoo", Category.BUILDING, "name" , "description");
//                Bukkit.getLogger().info("0");
//                MongodbServer.saveTicketAsync(t);
//
//            }, 100 + (i + 15));
//        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ConversationFactory getConversation(){
        return conversation;
    }

    public static Tickets getPlugin(){
        return instance;
    }
}
