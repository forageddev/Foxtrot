package dev.foraged.foxtrot.team

import com.cryptomorin.xseries.XMaterial
import dev.foraged.foxtrot.team.claim.Claim
import dev.foraged.foxtrot.team.claim.LandBoard
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

abstract class Team(override val identifier: UUID, var name: String, var color: ChatColor = ChatColor.WHITE) : IDataStoreObject
{
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[Team] "
        const val MAX_CLAIMS = 2
        val ALLY_COLOR = CC.AQUA
        val SELECTION_WAND = ItemBuilder(XMaterial.GOLDEN_HOE)
            .name("${CC.GREEN}Claiming Wand")
            .setLore(listOf("",
                "${CC.YELLOW}Right/Left Click${CC.GOLD} Block",
                "${CC.AQUA}- ${CC.WHITE}Select claim's corners",
                "",
                "${CC.YELLOW}Right Click §6Air",
                "${CC.AQUA}- ${CC.WHITE}Cancel current claim",
                "",
                "${CC.BLUE}Crouch ${CC.YELLOW}Left Click ${CC.GOLD}Block/Air",
                "${CC.AQUA}- ${CC.WHITE}Purchase current claim")).build()
    }

    val claims: MutableList<Claim> = mutableListOf()

    open fun ownsLocation(location: Location): Boolean {
        return LandBoard.getTeam(location) === this
    }

    open fun ownsClaim(claim: Claim?): Boolean {
        return claims.contains(claim)
    }

    abstract fun getName(player: Player) : String
    abstract fun saveEntry()
    abstract fun deleteEntry()
}