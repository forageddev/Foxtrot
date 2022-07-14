package dev.foraged.foxtrot.team.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import org.bukkit.entity.Player

class TeamRolePermissionsMenu : Menu("Team Role - Permissions")
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf()
    }
}