package dev.foraged.foxtrot.ui

import dev.foraged.foxtrot.classes.PvPClassHandler
import dev.foraged.foxtrot.classes.impl.ArcherClass
import dev.foraged.foxtrot.map.cooldown.OpplePersistableMap
import dev.foraged.foxtrot.map.cooldown.nopersist.EnderpearlMap
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import dev.foraged.foxtrot.map.stats.DeathsPersistMap
import dev.foraged.foxtrot.map.stats.KillsPersistMap
import dev.foraged.foxtrot.map.stats.KillstreakPersistMap
import dev.foraged.foxtrot.server.ServerHandler
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
    override fun getTitle(player: Player): String
    {
        return "${CC.B_PRI}${Bukkit.getServerName()} ${CC.WHITE}[Map One]"
    }

    override fun getLines(board: LinkedList<String>, player: Player)
    {
        board.add(CC.SB_BAR + "----")
        // if kitmap add stats
        if (ServerHandler.KIT_MAP) {
            board.add("${CC.B_PRI}Statistics")
            board.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Kills${CC.GRAY}: ${CC.RED}${KillsPersistMap[player.uniqueId] ?: 0}")
            board.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Deaths${CC.GRAY}: ${CC.RED}${DeathsPersistMap[player.uniqueId] ?: 0}")
            if ((KillstreakPersistMap[player.uniqueId] ?: 0) > 0) {
                board.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Streak${CC.GRAY}: ${CC.RED}${KillstreakPersistMap[player.uniqueId] ?: 0}")
            }
        }
        if (ServerHandler.SOTW_ACTIVE) {
            if (ServerHandler.SOTW_ENABLED.contains(player.uniqueId)) board.add("${CC.B_RED}Start Of The World:")
            else board.add("${CC.B_GREEN}Start Of The World:")
            board.add(" ${CC.GRAY}${Constants.DOT_SYMBOL} ${CC.RED}${formatDuration(ServerHandler.SOTW_EXPIRES)}")
        }

        if (PvPClassHandler.getPvPClass(player) != null) {
            board.addAll(PvPClassHandler.getPvPClass(player)!!.getScoreboardLines(player))
        }

        if (ArcherClass.isMarked(player)) {
            board.add("${CC.B_GOLD}Archer Mark${CC.GRAY}: ${CC.RED}${formatDuration(ArcherClass.getMarkedTime(player))}")
        }

        if (EnderpearlMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.B_YELLOW}Enderpearl${CC.GRAY}: ${CC.RED}${formatDuration(EnderpearlMap.getCooldown(player.uniqueId))}")
        }

        if (OpplePersistableMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.B_GOLD}Gopple${CC.GRAY}: ${CC.RED}${formatDuration(OpplePersistableMap.getCooldown(player.uniqueId))}")
        }

        if (SpawnTagMap.isOnCooldown(player.uniqueId)) {
            board.add("${CC.B_RED}Spawn Tag${CC.GRAY}: ${CC.RED}${formatDuration(SpawnTagMap.getCooldown(player.uniqueId))}")
        }

        board.add("")
        board.add("${CC.SEC}${Constants.SITE_LINK}")
        board.add(CC.SB_BAR + "----")
        if (board.size == 4) {
            board.clear()
        }
    }

    private fun formatDuration(long: Long?) : String {
        val seconds = ((long!! - System.currentTimeMillis()) / 1000).toInt()

        return if (seconds > 60) TimeUtil.formatIntoMMSS(seconds) else "${(10.0 * seconds.toDouble()).roundToInt() / 10.0}s"
    }
}