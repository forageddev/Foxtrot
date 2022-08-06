package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.chat.composite.TeamChatChannelComposite
import dev.foraged.foxtrot.team.TeamService
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@AutoRegister
@CommandAlias("telllocation|tl")
object TellLocationCommand : GoodCommand()
{
    @Default
    fun execute(sender: Player) {
        if (!TeamChatChannelComposite.canJoin(sender)) return

        val team = TeamService.findTeamByPlayer(sender.uniqueId) ?: return
        team.broadcast("${CC.BD_AQUA}[Team] ${CC.YELLOW}${sender.name}${CC.GRAY}: ${CC.WHITE}${sender.location.blockX}, ${sender.location.blockY}, ${sender.location.blockZ}")
    }
}