package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.DoublePersistMap

@RegisterMap
object BalancePersistMap : DoublePersistMap("Balance", "Balance", true)