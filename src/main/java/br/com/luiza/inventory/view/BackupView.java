package br.com.luiza.inventory.view;

import br.com.luiza.inventory.InventoryPlugin;
import br.com.luiza.inventory.model.InventoryData;
import me.saiintbrisson.minecraft.OpenViewContext;
import me.saiintbrisson.minecraft.View;
import me.saiintbrisson.minecraft.ViewContext;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BackupView extends View {

    private final InventoryPlugin plugin;

    public BackupView(InventoryPlugin plugin) {
        super(5);

        this.plugin = plugin;
    }

    @Override
    protected void onOpen(OpenViewContext context) {
        context.setInventoryTitle(StringUtils.EMPTY);
    }

    @Override
    protected void onRender(ViewContext context) {
        final InventoryData data = context.get("data");

        for (int i = 0; i < data.getItems().length; i++) {
            final ItemStack item = data.getItems()[i];
            if (item == null || item.getType() == Material.AIR) continue;

            context.slot(i).withItem(item);
        }
    }

    @Override
    protected void onClose(ViewContext context) {
        final InventoryData data = context.get("data");
        if (data == null) {
            throw new UnsupportedOperationException("Cannot update backup due the data map is wiped");
        }

        final ItemStack[] items = context.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            final ItemStack item = items[i];
            if (item == null || i > 39) break;

            data.getItems()[i] = item;
        }

        plugin.getService().submit(() -> plugin.getInventoryService().insert(data));
    }
}
