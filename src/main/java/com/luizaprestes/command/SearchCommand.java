package com.luizaprestes.command;

import com.celeste.event.EventWaiter;
import com.luizaprestes.InventoryPlugin;
import com.luizaprestes.model.InventoryData;
import com.luizaprestes.menu.BackupListMenu;
import com.luizaprestes.menu.BackupMenu;
import com.google.common.collect.ImmutableMap;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.annotation.Optional;
import me.saiintbrisson.minecraft.command.command.Context;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchCommand {

    private final InventoryPlugin plugin;

    public SearchCommand(InventoryPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(
      name = "backup.search"
    )
    public void handleSearchCommand(Context<Player> context, @Optional String username) {
        final Player sender = context.getSender();

        if (username == null) {
            sender.sendMessage(new String[] {
              " ",
              " §7Please type in the chat the username of the player.",
              " "
            });

            EventWaiter.of(AsyncPlayerChatEvent.class)
              .filter(event -> sender.equals(event.getPlayer()))
                .handler(event -> {
                    event.setCancelled(true);

                    final String message = event.getMessage();
                    acceptSearch(sender, message);
                });

            return;
        }

        acceptSearch(sender, username);
    }

    private void acceptSearch(Player player, String username) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
        if (offlinePlayer == null) {
            player.sendMessage("§cThe player " + username + " doesn't exist.");
            return;
        }

        final List<InventoryData> datas = CompletableFuture.supplyAsync(() -> plugin.getInventoryStorage().getByValue(
            offlinePlayer.getUniqueId()), plugin.getService()).join();

        switch (datas.size()) {
            case 0: {
                player.sendMessage("§cThe player " + username + " doesn't have any backups.");
                break;
            }
            case 1: {
                final BackupMenu backupMenu = new BackupMenu(plugin);
                backupMenu.show(player, ImmutableMap.of("data", datas.get(0)));
                break;
            }

            default: {
                final BackupListMenu backupListMenu = new BackupListMenu(plugin);
                backupListMenu.show(player, ImmutableMap.of("search", offlinePlayer.getUniqueId()));
            }
        }
    }
}
