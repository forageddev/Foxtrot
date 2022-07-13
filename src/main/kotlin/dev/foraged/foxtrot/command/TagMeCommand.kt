package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import org.bukkit.entity.Player

@CommandAlias("tagme")
@CommandPermission("foxtrot.tagme")
@AutoRegister
object TagMeCommand : GoodCommand()
{
    @Default
    fun execute(sender: Player, @Default("30") seconds: Int) {
        SpawnTagMap.startCooldown(sender.uniqueId, seconds)
    }
}