package dev.foraged.foxtrot.chat.part

import com.minexd.core.bukkit.chat.impl.GlobalChatChannelComposite
import dev.foraged.foxtrot.team.TeamService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.message.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent

object TeamPart : GlobalChatChannelComposite.ChatChannelPart("team", 5)
{
    override fun format(event: AsyncPlayerChatEvent): FancyMessage.SerializableComponent
    {
        val team = TeamService.findTeamByPlayer(event.player.uniqueId) ?: return FancyMessage.SerializableComponent("")
        val component = FancyMessage.SerializableComponent("${CC.GOLD}[${CC.YELLOW}${team.name}${CC.GOLD}] ")
        component.hoverMessage = "${CC.GREEN}Click to view team info."
        component.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team info ${team.name}")

        return component
    }
}