package dev.foraged.foxtrot.ability.menu

import com.cryptomorin.xseries.XMaterial
import dev.foraged.foxtrot.ability.AbilityService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class AbilityMenu : PaginatedMenu()
{
    override fun getPrePaginatedTitle(player: Player) = "Abilties"
    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            AbilityService.abilities.forEach { ability ->
                it[it.size] = object : Button() {
                    override fun getName(player: Player) = ability.value.displayName
                    override fun getDescription(player: Player) = ability.value.lore
                    override fun getMaterial(player: Player) = XMaterial.matchXMaterial(ability.value.type)
                    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                        player.chat("/ability give ${ability.key} ${1}")
                    }
                }
            }
        }
    }
}