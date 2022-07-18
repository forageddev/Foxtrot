package dev.foraged.foxtrot.ability

import dev.foraged.commons.persist.PluginService
import dev.foraged.foxtrot.ability.impl.SwitcherAbility
import gg.scala.flavor.service.Configure

object AbilityService : PluginService {
    private val abilities = mutableMapOf<String, Ability>()

    @Configure
    override fun configure() {
        registerAbility(SwitcherAbility)
    }

    fun registerAbility(ability: Ability) {
        abilities[ability.id] = ability
    }

    fun findAbility(id: String) : Ability? {
        return abilities[id]
    }
}