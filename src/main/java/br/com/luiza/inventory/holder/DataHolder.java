package br.com.luiza.inventory.holder;

import br.com.luiza.inventory.model.InventoryData;
import lombok.Data;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Data
public class DataHolder implements InventoryHolder {

    private final InventoryData data;

    @Override
    public Inventory getInventory() {
        return null;
    }
}
