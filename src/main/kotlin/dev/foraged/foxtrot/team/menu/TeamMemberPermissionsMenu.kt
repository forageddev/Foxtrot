package dev.foraged.foxtrot.team.menu

import com.cryptomorin.xseries.XMaterial
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberPermission
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class TeamMemberPermissionsMenu(val team: PlayerTeam, val member: TeamMember) : Menu("Team Member - Permissions")
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            TeamMemberPermission.values().forEach { permission ->
                it[it.size] = object : Button() {
                    override fun getName(player: Player): String {
                        return (if (member.hasPermission(permission)) CC.GREEN else CC.RED) + permission.displayName
                    }

                    override fun getDescription(player: Player): List<String>
                    {
                        return listOf("${CC.SEC}Click to the toggle the status", "${CC.SEC}of this permission for ${member.name}")
                    }

                    override fun getMaterial(player: Player): XMaterial
                    {
                        return if (member.hasPermission(permission)) XMaterial.DIAMOND else XMaterial.COAL
                    }

                    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
                    {
                        playSuccess(player)
                        if (team.hasPermission(member.uniqueId, permission)) team.permissions[member.uniqueId]!!.remove(permission)
                        else team.permissions[member.uniqueId]!!.add(permission)
                    }
                }
            }
        }
    }
}