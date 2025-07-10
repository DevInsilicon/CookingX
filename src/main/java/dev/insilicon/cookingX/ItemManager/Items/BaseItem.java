package dev.insilicon.cookingX.ItemManager.Items;

import dev.insilicon.cookingX.ItemManager.ItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class BaseItem implements Listener {
    private Material baseMaterial;

    private Component displayName;
    private String itemID;

    public BaseItem(Material baseMaterial, Component displayName, String itemID) {
        this.baseMaterial = baseMaterial;
        this.displayName = displayName;
        this.itemID = itemID;

    }

    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(baseMaterial);
        itemStack.editMeta(meta -> {
            meta.displayName(displayName);
            meta.getPersistentDataContainer().set(ItemManager.itemsKey, PersistentDataType.STRING, itemID);
        });
        return itemStack;
    }

    public void onItemInteraction(PlayerInteractEvent event) {
        
    }



}
