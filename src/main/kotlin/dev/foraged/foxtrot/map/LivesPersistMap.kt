package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap

@RegisterMap
object LivesPersistMap : IntegerPersistMap("Lives", "Lives", true)