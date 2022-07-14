package dev.foraged.foxtrot.team.data

enum class TeamMemberRole(val displayName: String, vararg val permissions: TeamMemberPermission)
{
    MEMBER("Member"),
    OFFICER("Officer",
        TeamMemberPermission.CREATE_INVITES,
        TeamMemberPermission.REVOKE_INVITES,
        TeamMemberPermission.UPDATE_HOME,
        TeamMemberPermission.KICK_MEMBER,
        TeamMemberPermission.WITHDRAW_BALANCE,
        TeamMemberPermission.ACCESS_SUBCLAIMS,
        TeamMemberPermission.UPDATE_ANNOUNCEMENT
    ),
    CO_LEADER("Co-Leader",
        TeamMemberPermission.DEMOTE_OFFICER,
        TeamMemberPermission.PROMOTE_OFFICER
    ),
    LEADER("Leader",
        TeamMemberPermission.PROMOTE_CO_LEADER,
        TeamMemberPermission.DEMOTE_CO_LEADER,
        TeamMemberPermission.UNCLAIM_LAND
    )
}