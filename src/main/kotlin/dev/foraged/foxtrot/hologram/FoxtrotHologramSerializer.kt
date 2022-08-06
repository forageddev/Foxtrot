package dev.foraged.foxtrot.hologram

import com.google.gson.*
import dev.foraged.commons.persist.impl.IntegerPersistMap
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.Location
import java.lang.reflect.Type

object FoxtrotHologramSerializer : JsonSerializer<FoxtrotHologram>, JsonDeserializer<FoxtrotHologram>
{
    override fun serialize(hologram: FoxtrotHologram, p1: Type, p2: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("implementation", hologram::class.qualifiedName)
        obj.addProperty("map", hologram.hologramMap::class.qualifiedName)
        obj.addProperty("title", hologram.title)
        obj.addProperty("location", Serializers.gson.toJson(hologram.location))
        return obj
    }

    override fun deserialize(element: JsonElement, p1: Type, p2: JsonDeserializationContext): FoxtrotHologram {
        val obj = element.asJsonObject

        val map = Class.forName(obj.get("map").asString)::class.objectInstance as IntegerPersistMap?

        return Class.forName(obj.get("implementation").asString).constructors[0].newInstance(
            map,
            obj.get("title").asString,
            Serializers.gson.fromJson(obj.get("location").asString, Location::class.java)
        ) as FoxtrotHologram
    }
}