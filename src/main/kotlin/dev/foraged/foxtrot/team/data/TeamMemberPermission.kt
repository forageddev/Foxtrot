package dev.foraged.foxtrot.team.data

import dev.foraged.foxtrot.team.TeamService
import java.util.*

enum class TeamMemberPermission(val displayName: String)
{
    CREATE_INVITES("Create Invitations"),
    REVOKE_INVITES("Revoke Invitations"),
    UPDATE_HOME("Update Home Location"),
    WITHDRAW_BALANCE("Withdraw Money"),
    ACCESS_SUBCLAIMS("Access Chest Subclaims"),
    OFFICER_CHAT("Officer Chat Access"),
    CLAIM_LAND("Claim Land"),
    UNCLAIM_LAND("Unclaim Land"),
    PROMOTE_OFFICER("Promote to Officer"),
    PROMOTE_CO_LEADER("Promote to Co-Leader"),
    DEMOTE_OFFICER("Demote from Officer"),
    DEMOTE_CO_LEADER("Demote from Co-Leader"),
    UPDATE_ANNOUNCEMENT("Update Announcement"),
    KICK_MEMBER("Kick Members");

    fun hasPermission(uuid: UUID) : Boolean {
        val team = TeamService.findTeamByPlayer(uuid) ?: return false
        return team.hasPermission(uuid, this)
    }
}