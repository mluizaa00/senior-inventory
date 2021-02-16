package com.luizaprestes;

import com.celeste.SQLProcessor;
import com.celeste.ServerPlugin;
import com.luizaprestes.command.BackupCommand;
import com.luizaprestes.command.SearchCommand;
import com.luizaprestes.model.InventoryData;
import com.luizaprestes.storage.InventoryStorage;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

@Getter
public class InventoryPlugin extends ServerPlugin {

    private ExecutorService service;
    private InventoryStorage inventoryStorage;

    private SQLProcessor sqlProcessor;

    @Override
    public void onLoad() {
        this.service = new ThreadPoolExecutor(
            2, 4,
            15, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder()
                .setNameFormat("Inventory Data Controller -  %s")
                .build()
        );

        this.sqlProcessor = new SQLProcessor(service);
        this.inventoryStorage = new InventoryStorage(sqlProcessor.getSqlConnectionProvider());
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        sqlProcessor.connect(
            getConfig().getString("mysql.hostname"),
            getConfig().getString("mysql.port"),
            getConfig().getString("mysql.database"),
            getConfig().getString("mysql.username"),
            getConfig().getString("mysql.password")
        );

        Executors.newSingleThreadScheduledExecutor()
          .scheduleWithFixedDelay(() -> {

              Bukkit.getOnlinePlayers().forEach(player -> {
                  final PlayerInventory inventory = player.getInventory();
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
                      player.getUniqueId(),
                      itemStacks,
                      Timestamp.from(Instant.now())
                  );

                  inventoryStorage.store(data);
              });

          }, 0L, 8, TimeUnit.HOURS);

        startCommandManager(
            "eua",
            new BackupCommand(),
            new SearchCommand(this)
        );

    }

}
