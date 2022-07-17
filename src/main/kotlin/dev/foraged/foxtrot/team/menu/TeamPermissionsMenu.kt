package dev.foraged.foxtrot.team.menu

import com.cryptomorin.xseries.XMaterial
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class TeamPermissionsMenu(val team: PlayerTeam) : Menu("Team - Permissions")
{
    override fun size(buttons: Map<Int, Button>): Int {
        return 9
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            it[3] = object : Button() {
                override fun getName(player: Player): String {
                    return "${CC.B_PRI}Role Permissions"
                }

                override fun getDescription(player: Player): List<String> {
                    return listOf("${CC.SEC}Modify permissions for roles", "${CC.SEC}in your team.")
                }

                override fun getMaterial(player: Player): XMaterial {
                    return XMaterial.CHEST
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                    TeamRoleSelectMenu(team) {
                        TeamRolePermissionsMenu(team, it).openMenu(player)
                    }.openMenu(player)
                }
            }

            it[5] = object : Button() {
                override fun getName(player: Player): String {
                    return "${CC.B_PRI}Player Permissions"
                }

                override fun getDescription(player: Player): List<String> {
                    return listOf("${CC.SEC}Modify permissions for members", "${CC.SEC}of your team.")
                }

                override fun getMaterial(player: Player): XMaterial {
                    return XMaterial.PLAYER_HEAD
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                    TeamMemberSelectMenu(team) {
                        TeamMemberPermissionsMenu(team, it).openMenu(player)
                    }.openMenu(player)
                }
            }
        }
    }
}