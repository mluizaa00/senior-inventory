package br.com.luiza.inventory.view;

import br.com.luiza.inventory.InventoryPlugin;
import br.com.luiza.inventory.model.InventoryData;
import br.com.luiza.inventory.utils.DateFormatter;
import com.google.common.collect.ImmutableMap;
import me.saiintbrisson.minecraft.*;
import me.saiintbrisson.minecraft.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class BackupListView extends PaginatedView<InventoryData> {

    private final InventoryPlugin plugin;

    public BackupListView(InventoryPlugin plugin) {
        super(6, "Backup list");

        this.plugin = plugin;
    }

    @Override
    protected void onRender(ViewContext context) {
        final UUID search = context.get("search");
        final List<InventoryData> data = plugin.getInventoryService().selectDatas(search);

        ((PaginatedViewContext<InventoryData>) context).setPaginationSource(data);
    }

    @Override
    protected void onPaginationItemRender(PaginatedViewContext<InventoryData> context, ViewItem item, InventoryData data) {
        item.withItem(item(data))
          .cancelOnClick()
          .onClick(action(data));
    }

    private ViewItemHandler action(InventoryData data) {
        return context -> {
            context.close();

            if(context.getClickOrigin().isLeftClick()) {
                context.open(BackupView.class, ImmutableMap.of(
                  "data", data
                ));
            } else {
                final OfflinePlayer offlinePlayer = data.getOfflinePlayer();
                if(!offlinePlayer.isOnline()) return;

                final Player player = offlinePlayer.getPlayer();
                data.apply(player.getInventory());
            }
        };
    }

    private ItemStack item(InventoryData data) {
        final OfflinePlayer offlinePlayer = data.getOfflinePlayer();

        final ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, 1, 3)
          .name("§e" + offlinePlayer.getName())
          .skull(offlinePlayer.getName())
          .lore(
            "§fCreated at: §7" + DateFormatter.format(data.getCreatedAt()),
            " ",
            "§eLeft click: Edit backup"
          );

        if(offlinePlayer.isOnline()) {
            builder.addLoreLine(
              "§eRight click: Apply backup"
            );
        }

        return builder.build();
    }

}
