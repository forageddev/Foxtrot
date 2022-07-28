package dev.foraged.foxtrot.team.impl

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberPermission
import dev.foraged.foxtrot.team.data.TeamMemberRole
import dev.foraged.foxtrot.team.dtr.RegenerationTask
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*


class PlayerTeam(identifier: UUID, name: String, val leader: TeamMember) : Team(identifier, name)
{
    var balance: Double = 0.0
    var deathsUntilRaidable: Double = 1.01
    val maxDeathsUntilRaidable : Double
        get() = RegenerationTask.getMaxDTR(size)
    var regenTime: Long = 0
    val members = mutableSetOf<TeamMember>()
    var home: Location? = null
    var announcement: String? = null
    val permissions = mutableMapOf<UUID, MutableList<TeamMemberPermission>>()
    val rolePermissions = mutableMapOf<TeamMemberRole, MutableList<TeamMemberPermission>>()
    val raidable: Boolean
        get() = deathsUntilRaidable < 0

    val officers: List<TeamMember>
        get() = members.filter { it.role == TeamMemberRole.OFFICER }

    val leaders: List<TeamMember>
        get() = members.filter { it.role == TeamMemberRole.CO_LEADER }

    val size = members.size + 1

    val onlineMemberCount: Int
        get() {
            var amt = 0
            for (member in members + leader)
            {
                val exactPlayer = member.getBukkitPlayer()
                if (exactPlayer != null && !exactPlayer.hasMetadata("invisible")) amt++
            }
            return amt
        }

    val onlineMembers: Collection<Player>
        get() {
            val players: MutableList<Player> = ArrayList()
            for (member in members)
            {
                val exactPlayer = member.getBukkitPlayer()
                if (exactPlayer != null && !exactPlayer.hasMetadata("invisible")) players.add(exactPlayer)
            }
            if (leader.getBukkitPlayer() != null) players.add(leader.getBukkitPlayer()!!)
            return players
        }

    val offlineMembers: Collection<UUID>
        get() {
            val players: MutableList<UUID> = ArrayList()
            for (member in members)
            {
                val exactPlayer = member.getBukkitPlayer()
                if (exactPlayer == null || exactPlayer.hasMetadata("invisible")) players.add(member.uniqueId)
            }
            return players
        }
    val invites = mutableSetOf<UUID>()

    init {
        for (role in TeamMemberRole.values()) {
            rolePermissions[role] = mutableListOf(*role.permissions)
        }
    }

    fun broadcast(message: String) {
        onlineMembers.forEach {
            it.sendMessage(CC.translate(message))
        }
    }

    fun getMember(uniqueId: UUID): TeamMember?
    {
        return allMembers.find { it.uniqueId == uniqueId }
    }

    fun isMember(uniqueId: UUID) : Boolean {
        return getMember(uniqueId) != null
    }

    fun isCaptain(uniqueId: UUID) : Boolean {
        return isMember(uniqueId) && getMember(uniqueId)!!.role == TeamMemberRole.OFFICER
    }

    fun isCoLeader(uniqueId: UUID) : Boolean {
        return isMember(uniqueId) && getMember(uniqueId)!!.role == TeamMemberRole.CO_LEADER
    }

    fun isOwner(uniqueId: UUID) : Boolean {
        return leader.uniqueId == uniqueId
    }

    fun hasPermission(role: TeamMemberRole, permission: TeamMemberPermission) : Boolean {
        val permissions = rolePermissions[role] ?: return false
        if (permissions.contains(permission)) return true
        return false
    }

    fun hasPermission(uniqueId: UUID, permission: TeamMemberPermission) : Boolean {
        if (uniqueId == leader.uniqueId) return true
        val member = getMember(uniqueId) ?: return false

        val permissions = this.permissions[uniqueId] ?: return false
        if (permissions.contains(permission)) return true
        return hasPermission(member.role, permission)
    }

    fun isAlly(uniqueId: UUID) : Boolean {
        // todo: implement allies to teamns.
        return false
    }

    val allMembers: Collection<TeamMember>
        get() {
            val members: MutableSet<TeamMember> = HashSet(members)
            members.add(this.leader)
            return members
        }

    override fun getName(player: Player): String
    {
        return if (isMember(player.uniqueId)) CC.GREEN + name
         else if (isAlly(player.uniqueId)) CC.LIGHT_PURPLE + name
        else CC.RED + name

    }

    fun getDTRIncrement(): Double
    {
        return getDTRIncrement(onlineMemberCount)
    }

    fun getDTRIncrement(playersOnline: Int): Double
    {
        val dtrPerHour: Double = RegenerationTask.getBaseDTRIncrement(size) * playersOnline
        return dtrPerHour / 60
    }

    fun playerDeath(playerName: String, dtrLoss: Double)
    {
        val newDTR = (deathsUntilRaidable - dtrLoss).coerceAtLeast(-.99)

        broadcast("${CC.RED}Member Death: ${CC.WHITE}$playerName")
        broadcast("${CC.RED}DTR: ${CC.WHITE}${DTR_FORMAT.format(newDTR)}")
        deathsUntilRaidable = newDTR

        deathsUntilRaidable = newDTR

        regenTime = System.currentTimeMillis() + 3 * 60 * 1000
        RegenerationTask.markOnDTRCooldown(this)
    }

    override fun saveEntry()
    {
        TeamService.playerTeamController.save(this, DataStoreStorageType.ALL)
    }

    override fun deleteEntry()
    {
        TeamService.playerTeamController.delete(identifier, DataStoreStorageType.ALL)
    }
}