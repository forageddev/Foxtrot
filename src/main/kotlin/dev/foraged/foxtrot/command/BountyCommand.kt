package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.BalancePersistMap
import dev.foraged.foxtrot.map.BountyPersistMap
import dev.foraged.foxtrot.server.MapService
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@AutoRegister
object BountyCommand : GoodCommand()
{
    @CommandAlias("bounty")
    fun pay(player: Player, target: UUID, amount: Double) {
        if (!MapService.KIT_MAP) throw ConditionFailedException("This command can only be executed on kits.")

        val targetPlayer = Bukkit.getPlayer(target) ?: throw ConditionFailedException("You cannot place money on offline players heads.")
        if (player == targetPlayer) throw ConditionFailedException("You cannot set a bounty on yourself.")
        if (amount <= 100) throw ConditionFailedException("You must set a minimum of $100 per bounty.")
        if (amount > 100_000) throw ConditionFailedException("You set a bounty worth more than $100,000.")

        if ((BalancePersistMap[player.uniqueId] ?: 0.0) < amount) throw ConditionFailedException("You cannot afford to set this bounty.")
        BalancePersistMap.minus(player.uniqueId, amount)
        BountyPersistMap.plus(target, amount)

        player.sendMessage("${CC.SEC}You have added ${CC.PRI}$amount ${CC.SEC} to ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}'s bounty.")
    }
}