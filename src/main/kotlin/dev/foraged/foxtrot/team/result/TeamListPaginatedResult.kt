package dev.foraged.foxtrot.team.result

import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.PaginatedResult
import net.md_5.bungee.api.chat.ClickEvent

object TeamListPaginatedResult : PaginatedResult<PlayerTeam>()
{
    override fun getHeader(page: Int, maxPages: Int) = "${CC.PRI}=== ${CC.SEC}Team List ${CC.WHITE}($page/$maxPages) ${CC.PRI}==="

    override fun format(result: PlayerTeam, resultIndex: Int): String {
        return " ${CC.PRI}${resultIndex + 1} ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.PRI}${result.name} ${CC.SEC}(${result.onlineMemberCount}/${result.size})"
    }

    override fun formatFancy(result: PlayerTeam, resultIndex: Int): FancyMessage {
        return super.formatFancy(result, resultIndex)
            .andHoverOf("${CC.SEC}DTR: ${CC.PRI}${result.deathsUntilRaidable}\n${CC.GREEN}Click to view team info")
            .andCommandOf(ClickEvent.Action.RUN_COMMAND, "/team info ${result.name}")
    }
}