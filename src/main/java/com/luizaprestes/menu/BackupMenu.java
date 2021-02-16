package com.luizaprestes.menu;

import com.celeste.menu.Menu;
import com.celeste.menu.MenuHolder;
import com.luizaprestes.InventoryPlugin;
import com.luizaprestes.model.InventoryData;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class BackupMenu extends Menu {

    private final InventoryPlugin plugin;

    public BackupMenu(InventoryPlugin plugin) {
        super(StringUtils.EMPTY, 9 * 5);

        this.plugin = plugin;
    }

    @Override
    protected void onRender(Player player, MenuHolder holder) {
        final InventoryData data = holder.getProperty("data");

        for (int i = 0; i < data.getItems().length; i++) {
            final ItemStack item = data.getItems()[i];
            if (item == null || item.getType() == Material.AIR) continue;

            holder.slot(i, item);
        }

    }

    @Override
    protected void onClose(InventoryCloseEvent event, MenuHolder holder) {
        final InventoryData data = holder.getProperty("data");

        if (data == null) {
            throw new UnsupportedOperationException("Cannot update backup due the data map is wiped");
        }

        final ItemStack[] items = holder.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            final ItemStack item = items[i];
            if (item == null || i > 39) break;

            data.getItems()[i] = item;
        }

        plugin.getService().submit(() -> plugin.getInventoryStorage().store(data));
    }

}
