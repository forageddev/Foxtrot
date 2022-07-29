package dev.foraged.foxtrot.game.koth

import dev.foraged.foxtrot.game.Game
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class KothGame(
    name: String,
    val height: Int,
    val captureZone: Cuboid,
    var captureTime: Long,
    override var active: Boolean = false
) : Game(name = name) {
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[KingOfTheHill] "
        val SECONDS_FORMATTER = DecimalFormat("#0.0")
    }

    var startTime: Long = 0
    var time: Long = 0
    var graceTime: Long = System.currentTimeMillis()
    val remainingMillis: Long get() = time + captureTime - System.currentTimeMillis()
    val grace: Boolean get() = System.currentTimeMillis() < graceTime
    val finished: Boolean get() = active && time + captureTime - (System.currentTimeMillis() + 1000) <= 0 && controllingPlayer != null

    var controllingPlayer: Player? = null
        set(value)
        {
            if (value == null && !finished) {
                Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The game ${CC.PRI}${name}${CC.SEC} has been knocked. ${CC.GRAY}(${formatTimeRemaining()})")
                graceTime = System.currentTimeMillis() + 5000
            } else {
                Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The game ${CC.PRI}${name}${CC.SEC} is now being contested. ${CC.GRAY}(${formatTimeRemaining()})")
            }

            time = System.currentTimeMillis()
            field = value
        }

    override fun start() {
        active = true
        startTime = System.currentTimeMillis()
        Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The game ${CC.PRI}${name}${CC.SEC} can now be contested. ${CC.GRAY}(${formatTimeRemaining()})")
    }

    override fun stop(winner: Player?) {
        if (controllingPlayer != null)
        {
            Bukkit.broadcastMessage(
                "$CHAT_PREFIX${CC.SEC}The game ${CC.PRI}${name}${CC.SEC} has been captured by ${
                    controllingPlayer!!.name
                } after being contestable for ${CC.PRI}${TimeUtil.formatIntoDetailedString(((System.currentTimeMillis() - startTime) / 1000).toInt())}${CC.SEC}."
            )
        }

        controllingPlayer = null
        active = false
        startTime = 0
        graceTime = 0
        time = 0
    }

    override fun getScoreboardLines(): List<String>
    {
        return listOf("${if (name.contains("Citadel")) CC.BD_PURPLE else CC.B_BLUE}${name}${CC.GRAY}: ${CC.RED}${formatTimeRemaining()}")
    }

    fun formatTimeRemaining() : String {
        val duration = if (controllingPlayer == null) captureTime else remainingMillis

        return if (duration > 3600000) TimeUtil.formatIntoAbbreviatedString((duration  / 1000).toInt())
        else if (duration > 60000) TimeUtil.formatIntoMMSS((duration / 1000).toInt())
        else SECONDS_FORMATTER.format(duration / 1000).replace(".0", "s")
    }
}