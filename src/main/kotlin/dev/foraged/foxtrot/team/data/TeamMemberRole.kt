package dev.foraged.foxtrot.team.data

enum class TeamMemberRole(vararg val permissions: TeamMemberPermission)
{
    MEMBER,
    OFFICER(
        TeamMemberPermission.CREATE_INVITES,
        TeamMemberPermission.REVOKE_INVITES,
        TeamMemberPermission.UPDATE_HOME,
        TeamMemberPermission.KICK_MEMBER,
        TeamMemberPermission.WITHDRAW_BALANCE,
        TeamMemberPermission.ACCESS_SUBCLAIMS,
        TeamMemberPermission.UPDATE_ANNOUNCEMENT
    ),
    CO_LEADER(
        TeamMemberPermission.DEMOTE_OFFICER,
        TeamMemberPermission.PROMOTE_OFFICER
    ),
    LEADER(
        TeamMemberPermission.PROMOTE_CO_LEADER,
        TeamMemberPermission.DEMOTE_CO_LEADER,
        TeamMemberPermission.UNCLAIM_LAND
    )
}