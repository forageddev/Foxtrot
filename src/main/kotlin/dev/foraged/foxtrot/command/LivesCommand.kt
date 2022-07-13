package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.LivesPersistMap
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

@AutoRegister
@CommandAlias("lives|life")
object LivesCommand : GoodCommand()
{
    @Default
    @Subcommand("check")
    @Description("Check how many lives you have")
    fun check(player: Player) {
        val lives = LivesPersistMap[player.uniqueId] ?: 0
        player.sendMessage("${CC.SEC}You currently have ${CC.RED}${lives}${Constants.HEART_SYMBOL} ${CC.SEC}${TextUtil.pluralize(lives, "life", "lives")}.")
    }

    @Subcommand("revive")
    @Description("Use your lives to revive other players")
    fun revive(player: Player, target: UUID) {
        if ((LivesPersistMap[player.uniqueId] ?: 0) == 0) throw ConditionFailedException("You do not have any lives available.")

        LivesPersistMap.minus(player.uniqueId, 1)
        //todo: remove deathban
        player.sendMessage("${CC.SEC}You have used ${CC.RED}1 ${Constants.HEART_SYMBOL} ${CC.SEC}life to revive ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.")
    }

    @Subcommand("set|update|=")
    @CommandPermission("foxtrot.lives.management")
    @Description("Update a players lives")
    fun set(sender: CommandSender, target: UUID, amount: Int) {
        LivesPersistMap[target] = amount
        sender.sendMessage("${CC.SEC}You have set ${CC.RED}${amount}${Constants.HEART_SYMBOL} ${CC.SEC}${
            TextUtil.pluralize(
                amount,
                "life",
                "lives"
            )
        } to ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.\"")
    }

    @Subcommand("add|plus|+")
    @CommandPermission("foxtrot.lives.management")
    @Description("Increase a players lives")
    fun add(sender: CommandSender, target: UUID, amount: Int) {
        LivesPersistMap.plus(target, amount)
        sender.sendMessage("${CC.SEC}You have added ${CC.RED}${amount}${Constants.HEART_SYMBOL} ${CC.SEC}${
            TextUtil.pluralize(
                amount,
                "life",
                "lives"
            )
        } to ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.\"")
    }

    @Subcommand("subtract|minus|-")
    @CommandPermission("foxtrot.lives.management")
    @Description("Subtract a players lives")
    fun subtract(sender: CommandSender, target: UUID, amount: Int) {
        LivesPersistMap.minus(target, amount)
        sender.sendMessage("${CC.SEC}You have taken ${CC.RED}${amount}${Constants.HEART_SYMBOL} ${CC.SEC}${
            TextUtil.pluralize(
                amount,
                "life",
                "lives"
            )
        } from ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.\"")
    }
}