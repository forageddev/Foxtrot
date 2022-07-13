package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("spawn")
@AutoRegister
object SpawnCommand : GoodCommand()
{
    @Default
    fun spawn(sender: Player, @Default("self") target: Player) {
        if (!sender.hasPermission("foxtrot.spawn")) throw ConditionFailedException("${Bukkit.getServerName()} does not have a spawn command! You must walk there!\n${CC.RED}Spawn is located at 0,0.")
        target.teleport(Bukkit.getServer().getWorld("world").spawnLocation)
    }
}