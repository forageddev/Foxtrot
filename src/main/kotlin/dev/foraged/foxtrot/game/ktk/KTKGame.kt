package dev.foraged.foxtrot.game.ktk

import dev.foraged.foxtrot.game.Game
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class KTKGame(name: String) : Game(name = name)
{
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[KillTheKing] "
    }

    override var active: Boolean = false
    val king: Player? = null

    override fun start() {
        Bukkit.broadcastMessage("")
        Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The King has awoken.")
        Bukkit.broadcastMessage("")
        active = true
    }

    override fun stop(winner: Player?) {
        Bukkit.broadcastMessage("")
        if (winner != null) Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The King has been slain by ${CC.PRI}${winner.name}${CC.SEC}.")
        else Bukkit.broadcastMessage("$CHAT_PREFIX${CC.SEC}The King has died.")
        Bukkit.broadcastMessage("")
    }
}