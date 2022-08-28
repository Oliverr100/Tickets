package me.bananababoo.tickets.GUI;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Sorts;
import me.bananababoo.tickets.Commands.TicketCommand;
import me.bananababoo.tickets.Database.MongodbServer;
import me.bananababoo.tickets.TicketStuff.Category;
import me.bananababoo.tickets.TicketStuff.Status;
import me.bananababoo.tickets.TicketStuff.Ticket;
import me.bananababoo.tickets.Tickets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.ne;

public class GUIManager {

    public static final GuiItem backgroundItem = generateItem("", Material.GRAY_STAINED_GLASS_PANE);

    public static PaginatedPane paginatedPane;

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
        GuiItem openFinishedTicketsWindowItem = generateItem("View Finished Tickets", Material.BOOKSHELF);
        openTicketWindowItem.setAction(event -> openTicketWindow(p, SortType.NORMAL));
        makeNewTicketItem.setAction(event -> {
            TicketCommand.create(p);
            p.closeInventory();
        });
        openFinishedTicketsWindowItem.setAction(event -> openTicketWindow(p, SortType.FINISHED));
        pane.bindItem('0', makeNewTicketItem);
        pane.bindItem('1', backgroundItem);
        pane.bindItem('2', openTicketWindowItem);
        pane.bindItem('3', openFinishedTicketsWindowItem);
        gui.addPane(pane);
        gui.show(p);
    }

    public static void openTicketWindow(Player p, SortType sort) {
        Bukkit.getScheduler().runTaskLater(Tickets.getPlugin(), () -> {


            //opens the tickets gui window and grabs all tickets from database and loads them in
            ChestGui gui = new ChestGui(6, "Tickets");
            PatternPane patternPane = getBottemPane(gui, p, false);

            gui.setOnGlobalClick(event -> event.setCancelled(true));


            MongodbServer.findTicketsAsync(docs -> {                //gets all documents in database, translates them into items, and puts into lists of 45 per page.
                SimpleDateFormat s = new SimpleDateFormat("MM-dd-yyyy");
                int amount = 0;
                FindIterable<Document> doc;
                try {
                    amount = docs.sort(Sorts.descending("id")).first().getInteger("id");
                } catch (NullPointerException ignored) {

                }
                switch (sort) {
                    case TIME_LATEST ->  doc = docs.sort(Sorts.descending("_id")).filter(ne("status", "FINISHED"));
                    case TIME_OLDEST -> doc = docs.sort(Sorts.ascending("_id")).filter(ne("status", "FINISHED"));
                    case PRIORITY -> doc = docs.filter(new Document("status", "HIGH_PRIORITY"));
                    case FINISHED -> doc = docs.filter(new Document("status", "FINISHED")).sort(Sorts.ascending("id"));
                    default -> doc = docs.sort(Sorts.ascending("id")).filter(ne("status", "FINISHED"));
                }
                for (int i = 0; i < (amount / 45) + 1; i++) {  // make enough pages
                    OutlinePane pane = new OutlinePane(0, 0, 9, 5);
                    doc.skip((i) * 45).limit(45).forEach((Consumer<Document>) document -> {
                        Ticket ticket = new Gson().fromJson(document.toJson(), Ticket.class);
                        ItemStack item = new ItemStack(ticket.category().mat);
                        ItemMeta meta = item.getItemMeta();
                        if (ticket.status().equals(Status.HIGH_PRIORITY)) {    //make a new item and if its high priority add enchants
                            meta.displayName(Component.text(ticket + " (High Priority)").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        } else {
                            meta.displayName(Component.text(ticket.toString()).color(NamedTextColor.GOLD));
                        }

                        List<Component> list = new ArrayList<>();
                        list.add(Component.text(ticket.description()).color(NamedTextColor.WHITE));
                        list.add(Component.empty());
                        list.add(Component.text("Category: " + ticket.category().toString()).color(NamedTextColor.GRAY));
                        list.add(Component.text(s.format(ticket.DateCreated()) + " " + ticket.playerName()).color(NamedTextColor.GRAY));
                        meta.lore(list);
                        item.setItemMeta(meta);
                        GuiItem guiItem = new GuiItem(item);
                        guiItem.setAction(event -> {
                            if (event.isShiftClick() && event.isRightClick()) { //if shift right click remove ticket and reset window while keeping same page
                                MongodbServer.removeTicketAsync(ticket);
                                p.sendMessage(Component.text("Removed ticket: " + ticket.name()).color(NamedTextColor.RED));
                                int page = paginatedPane.getPage();
                                openTicketWindow(p, SortType.NORMAL);
                                try {
                                    if (paginatedPane.getPages() > 0) {
                                        paginatedPane.setPage(page);
                                    }
                                    gui.update();
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                }
                            } else if (event.isRightClick() && !sort.equals(SortType.FINISHED)) {
                                if (ticket.status() != Status.HIGH_PRIORITY) {
                                    ticket.setStatus(Status.HIGH_PRIORITY);
                                    p.sendMessage(Component.text("Ticket " + ticket.name() + " set as High Priorty").color(NamedTextColor.GOLD));
                                } else {
                                    ticket.setStatus(Status.NORMAL);
                                    p.sendMessage(Component.text("Ticket " + ticket.name() + " set as Normal Priorty").color(NamedTextColor.GOLD));
                                }
                                MongodbServer.saveTicketAsync(ticket);
                                int page = paginatedPane.getPage();
                                openTicketWindow(p, SortType.NORMAL);
                                try {
                                    if (paginatedPane.getPages() > 0) {
                                        paginatedPane.setPage(page);
                                    }
                                    gui.update();
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                }
                                gui.update();
                            } else if (event.isShiftClick() && event.isLeftClick()) {
                                if (ticket.status() != Status.FINISHED) {
                                    ticket.setStatus(Status.FINISHED);
                                } else {
                                    ticket.setStatus(Status.NORMAL);
                                }
                                MongodbServer.saveTicketAsync(ticket);
                                Bukkit.getServer().broadcast(Component.text(p.getName() + " Finished ticket: " + ticket.name()).color(TextColor.color(95, 199, 121)));
                                int page = paginatedPane.getPage();
                                openTicketWindow(p, SortType.NORMAL);
                                try {
                                    if (paginatedPane.getPages() > 0) {
                                        paginatedPane.setPage(page);
                                    }
                                    gui.update();
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                }
                            }
                        });
                        pane.addItem(guiItem);
                    });
                    paginatedPane.addPane(i + 1, pane);
                }
                gui.addPane(paginatedPane);
                if (sort.equals(SortType.FINISHED)) {
                    paginatedPane.setPage(1);
                    gui.setTitle("Finished Tickets");
                    gui.addPane(getBottemPane(gui, p, true));
                } else {
                    paginatedPane.setPage(1);
                    gui.addPane(patternPane);
                }
                gui.show(p);
            });
        }, 10);
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
        GuiItem lore = generateItem("Plugin Config", Category.LORE.mat);
        GuiItem playTesting = generateItem("Idea", Category.PLAY_TESTING.mat);
        GuiItem technical = generateItem("nerd stuff", Category.TECHNICAL.mat);

        for(Category cat: Category.values()){
            GuiItem cate = generateItem(cat.name, cat.mat);
            cate.setAction(event -> {
                MongodbServer.saveTicketAsync(new Ticket(p.getName(), Category.BUILDING, name, description));
                p.sendMessage(Component.text("Ticket " + name + " Saved").color(TextColor.color(95, 199, 121)));
                openTicketWindow(p, SortType.NORMAL);
            });

        }


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

    public static PatternPane getBottemPane(Gui gui, Player p, boolean finished) {
        Pattern pattern;
        if (!finished) {
            pattern = new Pattern(
                    "310567024"
            );
        } else {
            pattern = new Pattern(
                    "310000024"
            );
        }

        paginatedPane = new PaginatedPane(0, 0, 9, 5);
        PatternPane patternPane = new PatternPane(0,5,9,1, pattern);
        GuiItem previousPage = generateItem("Previous Page", Material.PAPER);
        GuiItem nextPage = generateItem("Next Page", Material.PAPER);
        GuiItem goBack = generateItem("Go Back", Material.BARRIER);
        GuiItem controls;
        if(finished){
            controls = generateItem("Controls", Material.BOOK, List.of(Component.text("Set Ticket as Normal: Shift Left Click")));
        }else {
            controls = generateItem("Controls", Material.BOOK, List.of(
                    Component.text("Remove Ticket: Shift Right Click"),
                    Component.text("Toggle Ticket as High Priority: Right Click"),
                    Component.text("Toggle Ticket as Finished: Shift Left Click")
            ));
        }
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
        return patternPane;
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
