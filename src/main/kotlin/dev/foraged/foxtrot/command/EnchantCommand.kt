package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.enchant.Enchant
import dev.foraged.foxtrot.enchant.EnchantHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("enchant")
@CommandPermission("foxtrot.enchant.management")
@AutoRegister
object EnchantCommand : GoodCommand()
{
    @CommandManagerCustomizer
    fun customizer(manager: CommandManager) {
        manager.commandContexts.registerContext(Enchant::class.java) {
            val enchant = it.popFirstArg().lowercase()
            return@registerContext EnchantHandler.findEnchant(enchant) ?: throw ConditionFailedException("There is no enchant registered with the id \"${enchant}\".")
        }
    }

    @Subcommand("star")
    fun star(sender: CommandSender, enchant: Enchant, level: Int, amount: Int, target: Player) {
        target.inventory.addItem(ItemBuilder.copyOf(enchant.buildEnchantStar(level)).amount(amount).build())
        sender.sendMessage("${CC.SEC}You have given ${target.displayName} ${CC.PRI}$amount${CC.SEC} level ${CC.PRI}${level} ${enchant.displayName}${CC.SEC} enchant star.")
    }
}