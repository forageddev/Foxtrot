package dev.foraged.foxtrot.map

import com.minexd.core.bukkit.chat.ChatChannelComposite
import com.minexd.core.bukkit.chat.ChatChannelProvider
import com.minexd.core.bukkit.chat.ChatChannelService
import com.minexd.core.bukkit.chat.impl.GlobalChatChannelComposite
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.EnumerablePersistMap
import dev.foraged.foxtrot.chat.ChatMode
import dev.foraged.foxtrot.chat.part.TeamPart
import org.bukkit.Bukkit
import java.util.*

@RegisterMap
object ChatModePersistMap : EnumerablePersistMap<ChatMode>("ChatModes", "ChatMode", true, Bukkit.getServerName()),
    ChatChannelProvider
{
    override fun findChannel(uuid: UUID): ChatChannelComposite?
    {
        return this[uuid]?.composite
    }

    override fun findChannel(id: String): ChatChannelComposite?
    {
        return runCatching {
            ChatMode.valueOf(id).composite
        }.getOrNull()
    }

    override fun getKotlinObject(str: String?): ChatMode
    {
        if (str == null) return ChatMode.GLOBAL
        return ChatMode.valueOf(str)
    }

    init {
        GlobalChatChannelComposite.registerPart(TeamPart)
        ChatChannelService.registerProvider(this)
    }
}