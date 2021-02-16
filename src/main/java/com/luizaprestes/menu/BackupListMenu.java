package com.luizaprestes.menu;

import com.celeste.menu.Menu;
import com.celeste.menu.MenuHolder;
import com.celeste.menu.action.ClickAction;
import com.celeste.menu.paginator.Paginator;
import com.celeste.util.item.ItemBuilder;
import com.luizaprestes.InventoryPlugin;
import com.luizaprestes.model.InventoryData;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BackupListMenu extends Menu {

    private static final Integer[] SHAPE = new Integer[]{
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    private final InventoryPlugin plugin;

    public BackupListMenu(InventoryPlugin plugin) {
        super("Backup List", 9 * 6);

        this.plugin = plugin;
    }

    private String format(Date date) {
        return DATE_FORMAT.format(date);
    }

    @Override
    protected void onRender(Player player, MenuHolder holder) {
        final UUID search = holder.getProperty("search");
        final int page = holder.getProperty("page");

        final List<InventoryData> datas = plugin.getInventoryStorage().getByValue(search);

        final Paginator<InventoryData> paginator = new Paginator<>(SHAPE.length, new ArrayList<>(datas));
        final List<InventoryData> dataList = paginator.getPage(page);

        for (int i = 0; i < dataList.size(); i++) {
            final InventoryData data = dataList.get(i);

            final int slot = SHAPE[i];
            final ItemStack itemStack = item(data);

            holder.slot(slot, itemStack).withAction(action(data, player));
        }

        if (page > 0) {
            holder.slot(18, previousItem())
                .setPropertyOnClick("page", page - 1)
                .reopenOnClick();
        }

        if (datas.size() > (page + 1) * SHAPE.length) {
            holder.slot(26, nextItem())
                .setPropertyOnClick("page", page + 1)
                .reopenOnClick();
        }

    }

    private ItemStack item(InventoryData data) {
        final OfflinePlayer player = data.getOfflinePlayer();
        final ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, 1, 3)
            .name("§eData from " + player.getName())
            .skullOwner(player.getName())
            .lore(
                "§fCreated at: §7" + format(data.getCreationDate()),
                " ",
                "§eLeft click: Edit backup"
            );

        if (player.isOnline()) {
            builder.addLoreLine(
                "§eRight click: Apply backup"
            );
        }

        return builder.build();
    }

    private ClickAction action(InventoryData data, Player player) {
        return (holder, event) -> {
            if (event.getClick().isLeftClick()) {

                final BackupMenu backupMenu = new BackupMenu(plugin);
                backupMenu.show(player, ImmutableMap.of("data", data));

            } else {

                final OfflinePlayer offlinePlayer = data.getOfflinePlayer();
                if (!offlinePlayer.isOnline()) return;

                final Player target = offlinePlayer.getPlayer();
                data.apply(target.getInventory());

            }
        };
    }

    private ItemStack previousItem() {
        return new ItemBuilder(Material.ARROW)
            .name("§cPrevious page")
            .build();
    }

    private ItemStack nextItem() {
        return new ItemBuilder(Material.ARROW)
            .name("§cNext page")
            .build();
    }

}
