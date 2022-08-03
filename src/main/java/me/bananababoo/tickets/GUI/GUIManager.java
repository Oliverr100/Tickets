package me.bananababoo.tickets.GUI;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.google.gson.Gson;
import com.mongodb.client.model.Sorts;
import me.bananababoo.tickets.Commands.TicketCommand;
import me.bananababoo.tickets.Database.MongodbServer;
import me.bananababoo.tickets.TicketStuff.Category;
import me.bananababoo.tickets.TicketStuff.Status;
import me.bananababoo.tickets.TicketStuff.Ticket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GUIManager {

    public static final GuiItem backgroundItem = generateItem("", Material.GRAY_STAINED_GLASS_PANE);

    public static PaginatedPane paginatedPane;
    public static GuiItem lastguiItem;

    public static void openGUI(Player p) {          //opens main gui panel
        ChestGui gui = new ChestGui(3, "Tickets");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        Pattern pattern = new Pattern(
                "111111111",
                "101121131",
                "111111111"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        GuiItem openTicketWindowItem = generateItem("View Open Tickets", Material.PAPER);
        GuiItem makeNewTicketItem = generateItem("Make New Ticket", Material.WRITABLE_BOOK);
        GuiItem openFinishedTicketsWindowItem = generateItem("View Finished Tickets", Material.WRITABLE_BOOK);
        openTicketWindowItem.setAction(event -> openTicketWindow(p, SortType.NORMAL));
        makeNewTicketItem.setAction(event -> {
            TicketCommand.create(p);
            p.closeInventory();
        });
        openFinishedTicketsWindowItem.setAction(event -> openFinishedTicketWindow());
        pane.bindItem('0', makeNewTicketItem);
        pane.bindItem('1', backgroundItem);
        pane.bindItem('2', openTicketWindowItem);
        pane.bindItem('3', openFinishedTicketsWindowItem);
        gui.addPane(pane);
        gui.show(p);
    }

    public static void openTicketWindow(Player p, SortType sort){              //opens the tickets gui window and grabs all tickets from database and loads them in
        ChestGui gui = new ChestGui(6, "Tickets");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        Pattern pattern = new Pattern(
                "310567024"
        );
        paginatedPane = new PaginatedPane(0, 0, 9, 5);
        PatternPane patternPane = new PatternPane(0,5,9,1, pattern);
        GuiItem previousPage = generateItem("Previous Page", Material.PAPER);
        GuiItem nextPage = generateItem("Next Page", Material.PAPER);
        GuiItem goBack = generateItem("Go Back", Material.BARRIER);
        GuiItem controls = generateItem("Controls", Material.BOOK, List.of(
                Component.text("Remove Ticket: Shift Right Click"),
                Component.text("Toggle Ticket as High Priority: Right Click"),
                Component.text("Finish Ticket: Shift Left Click")
                ));
        GuiItem sortNewest = generateItem("Sort: Newest First", Material.GRASS_BLOCK);
        GuiItem sortOldest = generateItem("Sort: Oldest", Material.DIRT);
        GuiItem sortPriorty = generateItem("Sort: Priorty", Material.GOLD_BLOCK);
        previousPage.setAction(event -> {
            try {
                paginatedPane.setPage(paginatedPane.getPage() - 1);
                gui.update();
            } catch (ArrayIndexOutOfBoundsException ignored) {}
        });
        nextPage.setAction(event -> {
            try{
            paginatedPane.setPage(paginatedPane.getPage() + 1);
            gui.update();
            } catch (ArrayIndexOutOfBoundsException ignored) {}
        });
        goBack.setAction(event -> openGUI(p));
        sortNewest.setAction(event -> openTicketWindow(p, SortType.TIME_LATEST));
        sortOldest.setAction(event -> openTicketWindow(p, SortType.TIME_OLDEST));
        sortPriorty.setAction(event -> openTicketWindow(p, SortType.PRIORITY));
        patternPane.bindItem('0', backgroundItem);
        patternPane.bindItem('1', previousPage);
        patternPane.bindItem('2', nextPage);
        patternPane.bindItem('3', goBack);
        patternPane.bindItem('4', controls);
        patternPane.bindItem('5', sortNewest);
        patternPane.bindItem('6', sortOldest);
        patternPane.bindItem('7', sortPriorty);


        MongodbServer.findTicketsAsync(docs -> {                //gets all documents in database, translates them into items, and puts into lists of 45 per page.
            SimpleDateFormat s = new SimpleDateFormat("MM-dd-yyyy");
            int amount = 0;
            try {
                amount = docs.sort(Sorts.descending("id")).first().getInteger("id");
            } catch(Exception ignored){
                }
            switch(sort){
                case TIME_LATEST: docs = docs.sort(Sorts.descending("_id")); break;
                case TIME_OLDEST: docs = docs.sort(Sorts.ascending("_id")); break;
                case PRIORITY: docs = docs.filter(new Document("status", "HIGH_PRIORITY")).sort(Sorts.ascending("id")); break;
                case FINISHED: docs = docs.filter(new Document("status", "FINISHED")).sort(Sorts.ascending("id")); break;
                default: docs = docs.sort(Sorts.ascending("id")); break;
            }
            for(int i = -1; i < (amount / 45); i++){  // make enough pages
                OutlinePane pane = new OutlinePane(0, 0, 9, 5);
                docs.skip((i+1) * 45).limit(45).forEach((Consumer<Document>) document -> {
                    Ticket ticket = new Gson().fromJson(document.toJson(), Ticket.class);
                    ItemStack item = new ItemStack(ticket.category().mat);
                    ItemMeta meta = item.getItemMeta();
                    if(ticket.status().equals(Status.HIGH_PRIORITY)){
                        meta.displayName(Component.text(ticket + " (High Priority)").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }else {
                        meta.displayName(Component.text(ticket.toString()).color(NamedTextColor.GOLD));
                    }

                    List<Component> list = new ArrayList<>();
                    list.add(Component.text(ticket.description()).color(NamedTextColor.WHITE));
                    list.add(Component.empty());
                    list.add(Component.text("Category: " + ticket.category().toString()).color(NamedTextColor.GRAY));
                    list.add(Component.text(s.format(ticket.DateCreated()) + " " + ticket.playerName()).color(NamedTextColor.GRAY));
                    meta.lore(list);
                    item.setItemMeta(meta);
                    GuiItem guiItem  = new GuiItem(item);
                    guiItem.setAction(event -> {
                    if(event.isShiftClick() && event.isRightClick()){
                        MongodbServer.removeTicketAsync(ticket);
                        p.sendMessage(Component.text("Removed ticket " + ticket.name()).color(NamedTextColor.RED));
                        int page = paginatedPane.getPage();
                        openTicketWindow(p, SortType.NORMAL);
                            try{
                                if(paginatedPane.getPages() > 0) {
                                    paginatedPane.setPage(page);
                                    }
                                    gui.update();
                                } catch (ArrayIndexOutOfBoundsException ignored) {}
                            } else if (event.isRightClick()){
                                if(ticket.status() != Status.HIGH_PRIORITY) {
                                    ticket.setStatus(Status.HIGH_PRIORITY);
                                } else {
                                    ticket.setStatus(Status.NORMAL);
                                }
                                MongodbServer.saveTicketAsync(ticket);
                                p.sendMessage(Component.text("Ticket " + ticket.name() + " set as High Priorty").color(NamedTextColor.GOLD));
                                int page = paginatedPane.getPage();
                                openTicketWindow(p, SortType.NORMAL);
                                try{
                                    if(paginatedPane.getPages() > 0) {
                                        paginatedPane.setPage(page);
                                    }
                                    gui.update();
                                } catch (ArrayIndexOutOfBoundsException ignored) {}
                                gui.update();
                            }
                        });
                        pane.addItem(guiItem);
                        Bukkit.getLogger().info(item.toString());
                    });
                paginatedPane.addPane(i+2, pane);
                Bukkit.getLogger().info(Arrays.toString(paginatedPane.getPanes().toArray()) +  " itterator");
            }
            gui.addPane(paginatedPane);
            //gui.addPane(pane);
            gui.addPane(patternPane);
            paginatedPane.setPage(1);
            gui.show(p);
            Bukkit.getLogger().info("test");
        });
    }

    public static void chooseCategory(Player p, String name, String description){ // makes a gui that displays options to choose a category and then
        ChestGui gui = new ChestGui(3, "Choose a Category");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        Pattern pattern = new Pattern(
                "000000000",
                "010203040",
                "000000000"
        );
        PatternPane pane = new PatternPane(0, 0, 9, 3, pattern);
        GuiItem building = generateItem("Building", Category.BUILDING.mat);
        GuiItem lore = generateItem("Lore", Category.LORE.mat);
        GuiItem playTesting = generateItem("Play Testing", Category.PLAY_TESTING.mat);
        GuiItem technical = generateItem("nerd stuff", Category.TECHNICAL.mat);

        //TODO give category a type for name

        building.setAction(event -> {
            MongodbServer.saveTicketAsync(new Ticket(p.getName(), Category.BUILDING, name, description));
            p.sendMessage(Component.text("Ticket " + name + " Saved").color(TextColor.color(95, 199, 121)));
            openTicketWindow(p, SortType.NORMAL);
        });
        lore.setAction(event -> {
            MongodbServer.saveTicketAsync(new Ticket(p.getName(), Category.LORE, name, description));
            p.sendMessage(Component.text("Ticket " + name + " Saved").color(TextColor.color(95, 199, 121)));
            openTicketWindow(p, SortType.NORMAL);
        });
        playTesting.setAction(event -> {
            MongodbServer.saveTicketAsync(new Ticket(p.getName(), Category.PLAY_TESTING, name, description));
            p.sendMessage(Component.text("Ticket " + name + " Saved").color(TextColor.color(95, 199, 121)));
            openTicketWindow(p, SortType.NORMAL);
        });
        technical.setAction(event -> {
            MongodbServer.saveTicketAsync(new Ticket(p.getName(), Category.TECHNICAL, name, description));
            p.sendMessage(Component.text("Ticket " + name + " Saved").color(TextColor.color(95, 199, 121)));
            openTicketWindow(p, SortType.NORMAL);
        });



        pane.bindItem('0', backgroundItem);
        pane.bindItem('1', building);
        pane.bindItem('2', lore);
        pane.bindItem('3', playTesting);
        pane.bindItem('4', technical);

        gui.addPane(pane);
        gui.show(p);
    }


    public static void openFinishedTicketWindow(){

    }



    public static GuiItem generateItem(String name, Material material){  // makes a gui item from name and material
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return new GuiItem(item);
    }
    public static GuiItem generateItem(String name, Material material, List<Component> lore){  // makes a gui item from name and material
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(lore);
        item.setItemMeta(meta);
        return new GuiItem(item);
    }
}
