package dev.foraged.foxtrot.chat.composite

import com.minexd.core.bukkit.chat.ChatChannelComposite
import dev.foraged.foxtrot.team.TeamService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent

object AllianceChatChannelComposite : ChatChannelComposite {
    override fun canJoin(player: Player): Boolean {
        val team = TeamService.findTeamByPlayer(player.uniqueId)
        if (team == null) player.sendMessage("${CC.RED}You cannot join this channel whilst your not in a team.")
        return team != null
    }

    override fun id() = "alliance"
    override fun isGlobal() = false
    override fun buildMessage(event: AsyncPlayerChatEvent) = FancyMessage().withMessage("${CC.B_AQUA}[Ally] ${CC.D_AQUA}${event.player.name}${CC.GRAY}: ${CC.WHITE}${event.message}")
    override fun shouldReceive(event: AsyncPlayerChatEvent, player: Player): Boolean {
        val senderTeam = TeamService.findTeamByPlayer(event.player.uniqueId) ?: return false
        val team = TeamService.findTeamByPlayer(player.uniqueId) ?: return false
        return senderTeam.identifier == team.identifier
    }
}