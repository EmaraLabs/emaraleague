package com.emaralabs.emaraleague.core.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InventoryManager {

    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    public void saveAndClearInventory(Player player) {
        UUID playerId = player.getUniqueId();
        savedInventories.put(playerId, player.getInventory().getContents().clone());
        savedArmor.put(playerId, player.getInventory().getArmorContents().clone());
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
    }

    public void restoreInventory(Player player) {
        UUID playerId = player.getUniqueId();
        ItemStack[] contents = savedInventories.remove(playerId);
        ItemStack[] armor = savedArmor.remove(playerId);
        if (contents != null) {
            player.getInventory().setContents(contents);
        }
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
    }

    public boolean hasSavedInventory(UUID playerId) {
        return savedInventories.containsKey(playerId);
    }

    public void clearSavedInventory(UUID playerId) {
        savedInventories.remove(playerId);
        savedArmor.remove(playerId);
    }
}
