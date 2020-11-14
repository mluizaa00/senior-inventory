package br.com.luiza.inventory;

import br.com.luiza.inventory.command.BackupCommand;
import br.com.luiza.inventory.command.SearchCommand;
import br.com.luiza.inventory.database.MySQLConnector;
import br.com.luiza.inventory.model.InventoryData;
import br.com.luiza.inventory.service.InventoryService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import me.saiintbrisson.bukkit.command.BukkitFrame;
import me.saiintbrisson.minecraft.command.message.MessageHolder;
import me.saiintbrisson.minecraft.command.message.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

@Getter
public class InventoryPlugin extends JavaPlugin {

    private ExecutorService service;
    private InventoryService inventoryService;

    @Override
    @SneakyThrows
    public void onEnable() {
        saveDefaultConfig();

        final MySQLConnector connector = new MySQLConnector();
        connector.connect(
          getConfig().getString("database.jdbc_url"),
          getConfig().getString("database.username"),
          getConfig().getString("database.password")
        );

        registerCommands();

        this.service = new ThreadPoolExecutor(
          2, 4,
          15, TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(),
          new ThreadFactoryBuilder()
            .setNameFormat("Inventory Data Controller -  %s")
            .build()
        );

        this.inventoryService = new InventoryService(connector.getDataSource());

        Executors.newSingleThreadScheduledExecutor()
          .scheduleWithFixedDelay(() -> {

              for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                  final PlayerInventory inventory = onlinePlayer.getInventory();
                  final ItemStack[] itemStacks = new ItemStack[40];

                  for (int i = 0; i < 36; i++) {
                      itemStacks[i] = inventory.getItem(i);
                  }

                  itemStacks[36] = inventory.getBoots();
                  itemStacks[37] = inventory.getLeggings();
                  itemStacks[38] = inventory.getChestplate();
                  itemStacks[39] = inventory.getHelmet();

                  final InventoryData data = new InventoryData(
                    UUID.randomUUID(),
                    onlinePlayer.getUniqueId(),
                    itemStacks,
                    Timestamp.from(Instant.now())
                  );

                  inventoryService.insert(data);
              }
          }, 0L, 8, TimeUnit.HOURS);
    }

    private void registerCommands() {
        BukkitFrame frame = new BukkitFrame(this);
        MessageHolder messageHolder = frame.getMessageHolder();

        messageHolder.setMessage(MessageType.ERROR, "§cA error occurred.");
        messageHolder.setMessage(MessageType.INCORRECT_TARGET, "§cOnly players can execute this command..");
        messageHolder.setMessage(MessageType.INCORRECT_USAGE, "§cWrong use! The correct is: /{usage}");
        messageHolder.setMessage(MessageType.NO_PERMISSION, "§cYou don't have enough permissions.");

        frame.registerCommands(
          new BackupCommand(),
          new SearchCommand(this)
        );
    }
}
