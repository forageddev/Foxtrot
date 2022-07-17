package dev.foraged.foxtrot.ability

import dev.foraged.foxtrot.ability.impl.SwitcherAbility

object AbilityService {
    private val abilities = mutableMapOf<String, Ability>()

    init {
        registerAbility(SwitcherAbility)
    }

    fun registerAbility(ability: Ability) {
        abilities[ability.id] = ability
    }

    fun findAbility(id: String) : Ability? {
        return abilities[id]
    }
}