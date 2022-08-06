package dev.foraged.foxtrot.game.dtc

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.game.Game
import dev.foraged.foxtrot.game.GameService
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

class DTCGame(
    name: String,
    val point: Location,
    var points: Int = 100,
) : Game(
    name = name
), Listener
{
    var startTime = 0L
    var trackedPoints = mutableMapOf<UUID, Int>()
    var nextRegeneration: Long = 0
    override var active: Boolean = false

    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[DestroyTheCore] "
    }

    override fun start() {
        trackedPoints = mutableMapOf()
        nextRegeneration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)
        active = true
        startTime = System.currentTimeMillis()
        Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The game ${CC.PRI}${name}${CC.SEC} can now be contested. ${CC.GRAY}(${(points)})")
    }

    override fun getScoreboardLines(): List<String>
    {
        return listOf("${CC.WHITE}${name}: ${CC.GREEN}${points}")
    }

    override fun stop(winner: Player?)
    {
        if (winner != null)
        {
            Bukkit.broadcastMessage(
                "$CHAT_PREFIX${CC.SEC}The game ${CC.PRI}${name}${CC.SEC} has been destroyed by ${
                    winner.name
                } after being contestable for ${CC.PRI}${TimeUtil.formatIntoDetailedString(((System.currentTimeMillis() - startTime) / 1000).toInt())}${CC.SEC}."
            )
            Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}Top Three Destroyers: ")
            trackedPoints.entries.sortedByDescending { it.value }.forEachIndexed { i, it ->
                if (i > 2) return@forEachIndexed
                Bukkit.broadcastMessage("$CHAT_PREFIX${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}#${i + 1} ${CC.PRI}${ScalaStoreUuidCache.username(it.key)}${CC.SEC} mined the core ${CC.PRI}${it.value}${CC.SEC} times.")
            }
        }

        active = false
        points = 100
        nextRegeneration = 0
        startTime = 0
    }
}