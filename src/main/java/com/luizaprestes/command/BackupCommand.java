package com.luizaprestes.command;

import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import org.bukkit.entity.Player;

public class BackupCommand {

    @Command(
      name = "backup",
      permission = "backup.super"
    )
    public void handleLookupCommand(Context<Player> context) {
        context.sendMessage(new String[] {
            "",
            " §a§lBACKUP §8§l- §fHELP TOPIC",
            " ",
            " §a/backup search [<username>] §8- §7Search backup from player.",
            " "
        });
    }

}
