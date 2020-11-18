package br.com.luiza.inventory.command;

import br.com.luiza.inventory.InventoryPlugin;
import br.com.luiza.inventory.model.InventoryData;
import br.com.luiza.inventory.utils.EventScheduler;
import br.com.luiza.inventory.view.BackupListView;
import br.com.luiza.inventory.view.BackupView;
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
import java.util.concurrent.TimeUnit;

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
            sender.sendMessage(new String[]{
              " ",
              " §7Please type in the chat the username of the player.",
              " §7This action expires after 30 seconds.",
              " "
            });

            EventScheduler.of(plugin, AsyncPlayerChatEvent.class)
              .filter(event -> sender.equals(event.getPlayer()))
              .thenExecuteSync(event -> {
                  event.setCancelled(true);

                  final String message = event.getMessage();

                  acceptSearch(sender, message);

              }).orTimeOutAfter(30, TimeUnit.SECONDS, () -> {
                context.sendMessage("§cSearch expired.");
            }).schedule();
        } else {
            acceptSearch(sender, username);
        }
    }

    private void acceptSearch(Player player, String username) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
        if (offlinePlayer == null) {
            player.sendMessage("§cThe player " + username + " doesn't exist.");
            return;
        }

        final List<InventoryData> datas = CompletableFuture.supplyAsync(() -> {
            return plugin.getInventoryService().selectDatas(offlinePlayer.getUniqueId());
        }, plugin.getService()).join();

        switch (datas.size()) {
            case 0: {
                player.sendMessage("§cThe player " + username + " doesn't have any backups.");
                break;
            }
            case 1: {
                plugin.getViewFrame().open(BackupView.class, player, ImmutableMap.of(
                  "data", datas.get(0)
                ));
                break;
            }

            default: {
                plugin.getViewFrame().open(BackupListView.class, player, ImmutableMap.of(
                  "search", offlinePlayer.getUniqueId()
                ));
            }
        }
    }
}
