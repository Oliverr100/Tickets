package me.bananababoo.tickets.TicketStuff;

import org.bukkit.Material;

public enum Category {
    BUILDING (Material.BRICKS),
    LORE (Material.BOOKSHELF),
    TECHNICAL (Material.REPEATER),
    PLAY_TESTING (Material.IRON_SWORD);

    public final Material mat;

    private Category(Material mat) {
        this.mat = mat;
    }
}
