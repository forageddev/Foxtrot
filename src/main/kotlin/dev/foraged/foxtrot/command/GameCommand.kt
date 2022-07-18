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
import dev.foraged.foxtrot.game.koth.KothGame
import dev.foraged.foxtrot.game.result.GamePaginatedResult
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

    @Subcommand("koth create")
    @Description("Create a koth at your location")
    fun create(player: Player, name: String, duration: Duration) {
        if (GameService.findGameByName(name) != null) throw ConditionFailedException("A game with this game already exists.")

        GameService.registerGame(KothGame(name, player.location.blockY,
            Cuboid(player.location, player.getTargetBlock(setOf(Material.AIR), 15).location), duration.get()))
        player.sendMessage("${CC.SEC}You have created a new ${CC.PRI}KOTH${CC.SEC} with the name ${CC.PRI}${name}")
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

        game.stop()
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