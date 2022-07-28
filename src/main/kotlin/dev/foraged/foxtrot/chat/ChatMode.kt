package dev.foraged.foxtrot.chat

import com.minexd.core.bukkit.chat.ChatChannelComposite
import com.minexd.core.bukkit.chat.impl.GlobalChatChannelComposite
import dev.foraged.foxtrot.chat.composite.AllianceChatChannelComposite
import dev.foraged.foxtrot.chat.composite.OfficerChatChannelComposite
import dev.foraged.foxtrot.chat.composite.TeamChatChannelComposite
import dev.foraged.foxtrot.chat.part.TeamPart

enum class ChatMode(val prefix: Char, val composite: ChatChannelComposite)
{
    GLOBAL('!', GlobalChatChannelComposite),
    ALLIANCE('#', AllianceChatChannelComposite),
    TEAM('@', TeamChatChannelComposite),
    OFFICER('^', OfficerChatChannelComposite);

    companion object {
        fun findFromPrefix(prefix: Char) : ChatMode? {
            return values().firstOrNull {
                it.prefix == prefix
            }
        }
    }
}