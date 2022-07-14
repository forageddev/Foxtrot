package dev.foraged.foxtrot.team.menu

import com.cryptomorin.xseries.XMaterial
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberRole
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class TeamRoleSelectMenu(val team: PlayerTeam, val callback: (TeamMemberRole) -> Unit) : PaginatedMenu()
{
    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Team Role - Select"
    }

    override fun getButtonsStartOffset(): Int
    {
        return 3
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { 
            TeamMemberRole.values().filterNot { it == TeamMemberRole.LEADER }.forEach { role ->
                it[0] = object : Button() {
                    override fun getName(player: Player): String
                    {
                        return "${CC.GREEN}${role.displayName}"
                    }

                    override fun getDescription(player: Player): List<String>
                    {
                        return listOf("${CC.YELLOW}Click to choose ${role.displayName}")
                    }

                    override fun getMaterial(player: Player): XMaterial
                    {
                        return XMaterial.CHEST
                    }

                    override fun getButtonItem(player: Player): ItemStack
                    {
                        return ItemBuilder.copyOf(super.getButtonItem(player)).build()
                    }

                    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
                    {
                        callback.invoke(role)
                    }
                }
            }
        }
    }
}