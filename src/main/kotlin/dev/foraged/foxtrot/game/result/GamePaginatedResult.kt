package dev.foraged.foxtrot.game.result

import dev.foraged.foxtrot.game.Game
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.PaginatedResult
import net.evilblock.cubed.util.text.TextUtil

object GamePaginatedResult : PaginatedResult<Game>() {

    override fun format(result: Game, resultIndex: Int): String
    {
        return " ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.PRI}${result.name} ${CC.GRAY}(Active: ${TextUtil.stringifyBoolean(result.active, TextUtil.FormatType.YES_NO)}${CC.GRAY})"
    }

    override fun getHeader(page: Int, maxPages: Int) = "${CC.PRI}=== ${CC.SEC}Games ${CC.WHITE}($page/$maxPages) ${CC.PRI}==="
}