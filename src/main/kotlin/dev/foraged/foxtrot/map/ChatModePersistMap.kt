package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.EnumerablePersistMap
import dev.foraged.foxtrot.chat.ChatMode
import org.bukkit.Bukkit

@RegisterMap
object ChatModePersistMap : EnumerablePersistMap<ChatMode>("ChatModes", "ChatMode", true, Bukkit.getServerName())
{
    override fun getKotlinObject(str: String?): ChatMode
    {
        if (str == null) return ChatMode.PUBLIC
        return ChatMode.valueOf(str)
    }
}