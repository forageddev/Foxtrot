package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.BalancePersistMap
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

@AutoRegister
@CommandAlias("balance|eco|$|bal|money|economy")
object EconomyCommand : GoodCommand()
{
    @Default
    fun balance(player: Player, @Default("self") target: UUID) {
        player.sendMessage("${CC.GOLD}Balance of ${CC.WHITE}${ScalaStoreUuidCache.username(target)}${CC.GRAY}: ${CC.WHITE}${BalancePersistMap[target] ?: 0.0}")
    }

    @CommandAlias("pay|p2p")
    fun pay(player: Player, target: Player, amount: Double) {
        if (player == target) throw ConditionFailedException("You cannot send money to your own account.")
        if (amount < 0) throw ConditionFailedException("You cannot send a negative amount of money.")
        if (amount > 100_000) throw ConditionFailedException("You cannot more than $100,000 per transaction.")

        if ((BalancePersistMap[player.uniqueId] ?: 0.0) < amount) throw ConditionFailedException("You cannot afford to send this much money.")
        BalancePersistMap.minus(player.uniqueId, amount)
        BalancePersistMap.plus(target.uniqueId, amount)

        player.sendMessage("${CC.YELLOW}You have sent ${CC.LIGHT_PURPLE}$$amount${CC.YELLOW} to ${target.displayName}${CC.YELLOW}.")
        target.sendMessage("${CC.YELLOW}You have received ${CC.LIGHT_PURPLE}$$amount${CC.YELLOW} from ${player.displayName}${CC.YELLOW}.")
    }

    @CommandPermission("foxtrot.balance.management")
    @Subcommand("set")
    fun set(player: Player, target: UUID, amount: Double) {
        BalancePersistMap[target] = amount
        player.sendMessage("${CC.GOLD}New balance of ${CC.WHITE}${ScalaStoreUuidCache.username(target)}${CC.GRAY}: ${CC.WHITE}${amount}")
    }
}