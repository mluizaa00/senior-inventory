package br.com.luiza.inventory.command;

import br.com.luiza.inventory.InventoryPlugin;
import br.com.luiza.inventory.holder.DataHolder;
import br.com.luiza.inventory.model.InventoryData;
import br.com.luiza.inventory.utils.DateFormatter;
import br.com.luiza.inventory.utils.EventScheduler;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.annotation.Optional;
import me.saiintbrisson.minecraft.command.command.Context;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
                runSearch(player, datas.get(0));
                break;
            }

            default: {
                player.sendMessage(" §e§lBACKUPS");
                for (int i = 0; i < datas.size(); i++) {
                    final InventoryData data = datas.get(i);

                    player.sendMessage("§fID: §7"+ i + " §8| §fDate: §7" + DateFormatter.format(data.getCreatedAt()));
                }

                EventScheduler.of(plugin, AsyncPlayerChatEvent.class)
                  .filter(event -> {
                      if (!event.getPlayer().equals(player)) return false;

                      try {
                          Integer.parseInt(event.getMessage());
                      } catch (NumberFormatException e) {
                          player.sendMessage("§cYou sent a incorrect number, please try again.");
                          return false;
                      }

                      return true;
                  }).thenExecuteSync(event -> {
                    event.setCancelled(true);

                    final int i = Integer.parseInt(event.getMessage());
                    final InventoryData data = datas.get(i);

                    runSearch(player, data);

                }).orTimeOutAfter(30, TimeUnit.SECONDS, () -> {
                    player.sendMessage("§cSearch expired");
                }).schedule();
            }
            break;
        }
    }

    private void runSearch(Player player, InventoryData data) {
        final Inventory inventory = Bukkit.createInventory(
          new DataHolder(data),
          9 * 5,
          Bukkit.getOfflinePlayer(data.getPlayerId()).getName() + " - " + DateFormatter.format(data.getCreatedAt())
        );

        for (int i = 0; i < data.getItems().length; i++) {
            final ItemStack item = data.getItems()[i];
            if (item == null || item.getType() == Material.AIR) continue;

            inventory.setItem(i, item);
        }

        player.openInventory(inventory);
    }
}
