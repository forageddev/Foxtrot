package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Description
import dev.foraged.commons.acf.annotation.HelpCommand
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.game.Game
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.game.ctf.CTFGame
import dev.foraged.foxtrot.game.dtc.DTCGame
import dev.foraged.foxtrot.game.koth.KothGame
import dev.foraged.foxtrot.game.result.GamePaginatedResult
import dev.foraged.foxtrot.game.supply.SupplyDropService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import net.evilblock.cubed.util.time.Duration
import org.bukkit.Material
import org.bukkit.entity.Player

@AutoRegister
@CommandAlias("game|event|koth|conquest|citadel")
@CommandPermission("foxtrot.game.management")
object GameCommand : GoodCommand()
{
    @CommandManagerCustomizer
    fun customizer(manager: CommandManager) {
        manager.commandContexts.registerContext(Game::class.java) {
            val first = it.popFirstArg()
            GameService.findGameByName(first) ?: throw ConditionFailedException("There is no game registered with the name ${first}.")
        }
    }

    @Default
    @HelpCommand
    fun help(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("supplydrop drop")
    @Description("Drop a supply drop at your location")
    fun drop(player: Player, announce: Boolean) {
        SupplyDropService.summonSupplyDrop(announce)
        if (!announce) player.sendMessage("${CC.SEC}You have spawned a supply drop at your location.")
    }

    @Subcommand("supplydrop clearup")
    @Description("Clearup all supply drops on the ground")
    fun clearup(player: Player) {
        player.sendMessage("${CC.SEC}Cleaned up ${CC.PRI}${SupplyDropService.existingSupplyDrops.size}${CC.SEC} supply drops.")
        SupplyDropService.existingSupplyDrops.forEach(SupplyDropService::removeSupplyDrop)
    }

    @Subcommand("supplydrop update")
    @Description("Update the contents won for supply drops")
    fun update(player: Player) {
        SupplyDropService.contents = player.inventory.contents.filterNotNull().filterNot { it.type == Material.AIR }.toMutableList()
        player.sendMessage("${CC.SEC}You have updated the supply drop contents.")
    }

    @Subcommand("koth create")
    @Description("Create a koth at your location")
    fun create(player: Player, name: String, duration: Duration) {
        if (GameService.findGameByName(name) != null) throw ConditionFailedException("A game with this name already exists.")

        GameService.registerGame(
            KothGame(name, player.location.blockY,
            Cuboid(player.location, player.getTargetBlock(setOf(Material.AIR), 15).location), duration.get())
        )
        player.sendMessage("${CC.SEC}You have created a new ${CC.PRI}KOTH${CC.SEC} with the name ${CC.PRI}${name}")
    }

    @Subcommand("ctf create")
    @Description("Create a capture the flag at your location")
    fun createCtf(player: Player, name: String) {
        if (GameService.findGameByName(name) != null) throw ConditionFailedException("A game with this name already exists.")

        GameService.registerGame(CTFGame(name, player.location))
        player.sendMessage("${CC.SEC}You have created a new ${CC.PRI}CTF${CC.SEC} with the name ${CC.PRI}${name}")
    }

    @Subcommand("koth color")
    @Description("Change the color of a koth")
    fun create(player: Player, game: Game, color: String) {
        if (game !is KothGame) throw ConditionFailedException("You can only use this command on games that are instance of KothGame.")

        game.color = CC.translate(color)
        player.sendMessage("${CC.SEC}You have set the color of the koth ${CC.PRI}${name}${CC.SEC} to ${CC.translate(color)}Example${CC.SEC}.")
    }

    @Subcommand("dtc create")
    @Description("Create a dtc at your location")
    fun create(player: Player, name: String) {
        if (GameService.findGameByName(name) != null) throw ConditionFailedException("A game with this name already exists.")

        val block = player.getTargetBlock(setOf(Material.AIR), 5)
        if (block.type != Material.OBSIDIAN) throw ConditionFailedException("You can only create a destroy the core game whilst looking at a block of obsidian.")
        GameService.registerGame(DTCGame(name, block.location))
        player.sendMessage("${CC.SEC}You have created a new ${CC.PRI}DTC${CC.SEC} with the name ${CC.PRI}${name}")
        player.sendMessage("${CC.SEC}Core Location: ${CC.PRI}${block.location.blockX}, ${block.location.blockY}, ${block.location.blockZ}")
    }

    @Subcommand("list")
    @Description("Shows a list of created games")
    fun list(player: Player, @Default("1") page: Int) {
        GamePaginatedResult.display(player, GameService.games, page)
    }

    @Subcommand("start")
    @Description("Start a game with the provided name")
    fun start(player: Player, game: Game) {
        if (game.active) throw ConditionFailedException("${game.name} is not already active.")

        game.start()
        player.sendMessage("${CC.SEC}You started the game ${CC.PRI}${game.name}${CC.SEC}.")
    }

    @Subcommand("stop")
    @Description("Stop a game with the provided name")
    fun stop(player: Player, game: Game) {
        if (!game.active) throw ConditionFailedException("${game.name} is not currently active.")

        game.stop(null)
        player.sendMessage("${CC.SEC}You stopped the game ${CC.PRI}${game.name}${CC.SEC}.")
    }

    @Subcommand("delete")
    @Description("Delete a game with the provided name")
    fun delete(player: Player, game: Game) {
        if (game.active) throw ConditionFailedException("${game.name} cannot be deleted as it is currently active. You can stop it by using /game stop ${game.name}")

        GameService.deleteGame(game)
        player.sendMessage("${CC.SEC}You deleted the game ${CC.PRI}${game.name}${CC.SEC}.")
    }
}