package dev.foraged.foxtrot.map.ore.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.foxtrot.map.ore.OrePersistableMap
import net.evilblock.cubed.util.CC
import org.bukkit.Material

@Listeners
@RegisterMap
object EmeraldPersistableMap : OrePersistableMap("EmeraldMined", "MiningStats.Emerald", CC.GREEN, Material.EMERALD_ORE)