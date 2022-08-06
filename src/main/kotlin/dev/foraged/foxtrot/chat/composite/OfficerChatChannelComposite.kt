package dev.foraged.foxtrot.chat.composite

import com.minexd.core.bukkit.chat.ChatChannelComposite
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.data.TeamMemberPermission
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.message.FancyMessage
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent

object OfficerChatChannelComposite : ChatChannelComposite {
    override fun canJoin(player: Player): Boolean {
        val team = TeamService.findTeamByPlayer(player.uniqueId)
        if (team == null) player.sendMessage("${CC.RED}You cannot join this channel whilst your not in a team.")
        if (team!!.getMember(player.uniqueId)?.hasPermission(TeamMemberPermission.CLAIM_LAND) != true) {
            player.sendMessage("${CC.RED}You do not have permission to access this team chat channel.")
            return false
        }
        return true
    }

    override fun id() = "officer"
    override fun isGlobal() = false
    override fun buildMessage(event: AsyncPlayerChatEvent) = FancyMessage().withMessage("${CC.BL_PURPLE}[Officer] ${CC.YELLOW}${event.player.name}${CC.GRAY}: ${CC.WHITE}${event.message}")
    override fun shouldReceive(event: AsyncPlayerChatEvent, player: Player): Boolean {
        val senderTeam = TeamService.findTeamByPlayer(event.player.uniqueId) ?: return false
        val team = TeamService.findTeamByPlayer(player.uniqueId) ?: return false
        return senderTeam.identifier == team.identifier
    }
}