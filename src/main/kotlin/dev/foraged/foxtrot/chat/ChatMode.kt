package dev.foraged.foxtrot.chat

enum class ChatMode(val prefix: Char)
{
    PUBLIC('!'),
    ALLIANCE('#'),
    TEAM('@'),
    OFFICER('^');

    companion object {
        fun findFromPrefix(prefix: Char) : ChatMode? {
            return values().firstOrNull {
                it.prefix == prefix
            }
        }
    }
}