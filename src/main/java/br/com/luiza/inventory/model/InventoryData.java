package br.com.luiza.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class InventoryData {

    private final UUID id;
    private final UUID playerId;

    private final ItemStack[] items;
    private final Timestamp createdAt;

    public void apply(PlayerInventory inventory) {
        inventory.clear();

        for (int i = 0; i < items.length; i++) {
            final ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;

            inventory.setItem(i, item);
        }
    }
}
