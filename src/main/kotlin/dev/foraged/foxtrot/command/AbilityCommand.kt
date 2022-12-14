package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Description
import dev.foraged.commons.acf.annotation.HelpCommand
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.ability.Ability
import dev.foraged.foxtrot.ability.AbilityService
import dev.foraged.foxtrot.ability.menu.AbilityMenu
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("ability")
@CommandPermission("foxtrot.ability.management")
@AutoRegister
object AbilityCommand : GoodCommand()
{
    @CommandManagerCustomizer
    fun customizer(manager: CommandManager) {
        manager.commandContexts.registerContext(Ability::class.java) {
            val ability = it.popFirstArg().lowercase()
            return@registerContext AbilityService.findAbility(ability) ?: throw ConditionFailedException("There is no ability registered with the id \"${ability}\".")
        }
    }

    @HelpCommand
    fun help(commandHelp: CommandHelp) {
        commandHelp.showHelp()
    }

    @Subcommand("menu")
    @Description("Open the ability item menu")
    fun menu(sender: Player) {
        AbilityMenu().openMenu(sender)
    }

    @Subcommand("give")
    @Description("Give yourself an ability item")
    fun give(sender: CommandSender, ability: Ability, amount: Int, target: Player) {
        target.inventory.addItem(ability.getItem(amount))
        sender.sendMessage("${CC.SEC}You have given ${target.displayName} ${CC.PRI}$amount${CC.SEC} ${ability.displayName}${CC.SEC} ability items.")
    }
}