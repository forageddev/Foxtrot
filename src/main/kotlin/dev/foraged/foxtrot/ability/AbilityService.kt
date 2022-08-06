package dev.foraged.foxtrot.ability

import dev.foraged.commons.persist.PluginService
import dev.foraged.foxtrot.ability.impl.*
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import org.bukkit.inventory.ItemStack

@Service
object AbilityService : PluginService {
    val abilities = mutableMapOf<String, Ability>()

    @Configure
    override fun configure() {
        registerAbility(SwitcherAbility)
        registerAbility(SwitchStickAbility)
        registerAbility(ThorAxeAbility)
        registerAbility(BallOfTruthAbility)
        registerAbility(EyeBlinderAbility)
        registerAbility(AntiBuildStickAbility)
    }

    fun registerAbility(ability: Ability) {
        abilities[ability.id] = ability
    }
    fun findAbility(item: ItemStack) : Ability? = abilities.values.find { it.isAbilityItem(item) }
    fun findAbility(id: String) : Ability? = abilities[id]

}