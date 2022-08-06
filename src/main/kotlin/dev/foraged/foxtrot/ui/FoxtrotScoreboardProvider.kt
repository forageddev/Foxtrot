package dev.foraged.foxtrot.ui

import dev.foraged.foxtrot.classes.PvPClassService
import dev.foraged.foxtrot.classes.impl.ArcherClass
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.map.cooldown.OpplePersistableMap
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.map.cooldown.nopersist.*
import dev.foraged.foxtrot.map.stats.DeathsPersistMap
import dev.foraged.foxtrot.map.stats.KillsPersistMap
import dev.foraged.foxtrot.map.stats.KillstreakPersistMap
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.roundToInt

object FoxtrotScoreboardProvider : ScoreboardAdapter()
{
    override fun getInterval(): Long
    {
        return 2L
    }

    override fun getTitle(player: Player): String
    {
        return "${CC.B_PRI}${Bukkit.getServerName()} ${CC.WHITE}[Map One]"
    }

    override fun getLines(board: LinkedList<String>, player: Player)
    {
        board.add(CC.SB_BAR + "----")
        // if kitmap add stats
        if (MapService.KIT_MAP) {
            board.add("${CC.B_PRI}Statistics")
            board.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}Kills: ${CC.PRI}${KillsPersistMap[player.uniqueId] ?: 0}")
            board.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}Deaths: ${CC.PRI}${DeathsPersistMap[player.uniqueId] ?: 0}")
            if ((KillstreakPersistMap[player.uniqueId] ?: 0) > 0) {
                board.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}Streak: ${CC.PRI}${KillstreakPersistMap[player.uniqueId] ?: 0}")
            }
        }
        for (game in GameService.games) {
            if (game.active) board.addAll(game.getScoreboardLines())
        }

        if (MapService.SOTW_ACTIVE) {
            if (MapService.SOTW_ENABLED.contains(player.uniqueId)) board.add("${CC.WHITE}SOTW: ${CC.RED}${formatDuration(MapService.SOTW_EXPIRES)}")
            else board.add("${CC.WHITE}SOTW: ${CC.GREEN}${formatDuration(MapService.SOTW_EXPIRES)}")
        }

        if (MapService.EOTW_START_ACTIVE) {
            board.add("${CC.WHITE}EOTW: ${CC.RED}${formatDuration(MapService.EOTW_STARTS)}")
        }

        if (PvPClassService.getPvPClass(player) != null) {
            board.addAll(PvPClassService.getPvPClass(player)!!.getScoreboardLines(player))
        }

        if (ArcherClass.isMarked(player)) {
            board.add("${CC.WHITE}Mark: ${CC.GOLD}${formatDuration(ArcherClass.getMarkedTime(player))}")
        }

        if (EnderpearlMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Enderpearl: ${CC.YELLOW}${formatDuration(EnderpearlMap.getCooldown(player.uniqueId))}")
        }

        if (AbilityCooldownMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Ability: ${CC.AQUA}${formatDuration(AbilityCooldownMap.getCooldown(player.uniqueId))}")
        }

        if (AppleMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Apple: ${CC.YELLOW}${formatDuration(AppleMap.getCooldown(player.uniqueId))}")
        }

        if (LogoutMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Logout: ${CC.RED}${formatDuration(LogoutMap.getCooldown(player.uniqueId))}")
        }

        if (TeamStuckMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Stuck: ${CC.BD_RED}${formatDuration(TeamStuckMap.getCooldown(player.uniqueId))}")
        }

        if (TeamHomeMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Home: ${CC.BLUE}${formatDuration(TeamHomeMap.getCooldown(player.uniqueId))}")
        }

        if (OpplePersistableMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Gopple: ${CC.GOLD}${formatDuration(OpplePersistableMap.getCooldown(player.uniqueId))}")
        }

        if (SpawnTagMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}Spawn Tag: ${CC.RED}${formatDuration(SpawnTagMap.getCooldown(player.uniqueId))}")
        }

        if (PvPTimerPersistableMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.WHITE}PvP Timer: ${CC.GREEN}${formatDuration(PvPTimerPersistableMap.getCooldown(player.uniqueId))}")
        }

        board.add(CC.SB_BAR + "----")
        if (board.size == 2) {
            board.clear()
        }
    }

    fun formatDuration(long: Long?) : String {
        val seconds = ((long!! - System.currentTimeMillis()) / 1000).toInt()

        return TimeUtil.formatIntoAbbreviatedString(seconds)
        //return if (seconds > 60) TimeUtil.formatIntoMMSS(seconds) else "${((10.0 * seconds.toDouble()).roundToInt() / 10.0).toString().replace(".0", "")}s"
    }
}