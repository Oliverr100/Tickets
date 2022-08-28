package me.bananababoo.tickets.TicketStuff;

import org.bukkit.Material;

public enum Category {
    BUILDING (Material.BRICKS, "Building"),
    LORE (Material.BOOKSHELF, "Plugin Config"),
    TECHNICAL (Material.REPEATER,"Idea"),
    PLAY_TESTING (Material.IRON_SWORD,"nerd stuff");

    public final Material mat;
    public final String name;

    Category(Material mat, String name) {
        this.name = name;
        this.mat = mat;
    }
}
