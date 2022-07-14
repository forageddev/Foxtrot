package dev.foraged.foxtrot.team.menu

import com.cryptomorin.xseries.XMaterial
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberPermission
import dev.foraged.foxtrot.team.data.TeamMemberRole
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class TeamRolePermissionsMenu(val team: PlayerTeam, val role: TeamMemberRole) : Menu("Team Role - Permissions")
{
    init {
        updateAfterClick = true
        autoUpdate = true
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            TeamMemberPermission.values().forEach { permission ->
                it[it.size] = object : Button() {
                    override fun getName(player: Player): String {
                        return (if (team.hasPermission(role, permission)) CC.GREEN else CC.RED) + permission.displayName
                    }

                    override fun getDescription(player: Player): List<String>
                    {
                        return listOf("${CC.SEC}Click to the toggle the status", "${CC.SEC}of this permission for ${role.displayName}")
                    }

                    override fun getMaterial(player: Player): XMaterial
                    {
                        return if (team.hasPermission(role, permission)) XMaterial.DIAMOND else XMaterial.COAL
                    }

                    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
                    {
                        playSuccess(player)
                        if (team.hasPermission(role, permission)) team.rolePermissions[role]!!.remove(permission)
                        else team.rolePermissions[role]!!.add(permission)
                    }
                }
            }
        }
    }
}