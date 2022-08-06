package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.chat.ChatMode
import dev.foraged.foxtrot.event.team.TeamCreateEvent
import dev.foraged.foxtrot.map.BalancePersistMap
import dev.foraged.foxtrot.map.ChatModePersistMap
import dev.foraged.foxtrot.map.cooldown.nopersist.TeamHomeMap
import dev.foraged.foxtrot.map.cooldown.nopersist.TeamStuckMap
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.claim.ClaimService
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.claim.VisualClaim
import dev.foraged.foxtrot.team.claim.VisualClaimType
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberPermission
import dev.foraged.foxtrot.team.data.TeamMemberRole
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import dev.foraged.foxtrot.team.menu.TeamPermissionsMenu
import dev.foraged.foxtrot.team.result.TeamListPaginatedResult
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.menu.menus.SelectColorMenu
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.message.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.text.TextUtil
import net.evilblock.cubed.util.time.DateUtil
import net.evilblock.cubed.util.time.TimeUtil
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("team|t|f|faction|fac")
@AutoRegister
object TeamCommand : GoodCommand()
{
    @HelpCommand
    @Default
    @Description("View this team help message")
    fun help(help: CommandHelp) {
        help.showHelp()
    }

    @CommandManagerCustomizer
    fun customizer(manager: CommandManager) {
        manager.commandContexts.registerContext(Team::class.java) {
            val arg = it.popFirstArg()
            if (arg.equals("self", ignoreCase = true)) return@registerContext TeamService.findTeamByPlayer((it.sender as Player).uniqueId)
                ?: throw ConditionFailedException("You are not currently in a team. Please create a new team using /team create <name>")
            return@registerContext TeamService.findTeamByName(arg) ?: throw ConditionFailedException("Team with name or member $arg not found.")
        }
    }

    @Subcommand("create")
    @Description("Create a new team")
    fun create(player: Player, @Single @Conditions("validate:min=3,max=16,regex=[^a-zA-Z0-9]") name: String) {
        if (TeamService.findTeamByName(name) != null) throw ConditionFailedException("Another team with the name $name already exists. Please choose a new name for your team.")
        if (TeamService.findTeamByPlayer(player.uniqueId) != null) throw ConditionFailedException("You are already in another team. Please leave your existing team before attempting to perform this command again.")

        val team = PlayerTeam(UUID.randomUUID(), name, TeamMember(player.uniqueId, player.name, TeamMemberRole.LEADER))
        if (!TeamCreateEvent(team).call()) throw ConditionFailedException("Your team creation was cancelled.")

        TeamService.registerTeam(team)
        Bukkit.broadcastMessage("${Team.CHAT_PREFIX}${CC.PRI}$name${CC.SEC} has been ${CC.GREEN}opened${CC.SEC} by ${CC.PRI}${player.displayName}")
    }

    @Subcommand("createsystem")
    @CommandPermission("foxtrot.team.management")
    @Description("Create a new system team")
    fun createSystem(player: Player, name: String) {
        TeamService.registerTeam(SystemTeam(UUID.randomUUID(), name))
        player.sendMessage("${CC.SEC}Created system team with name ${CC.PRI}${name}${CC.SEC}.")
    }

    @Subcommand("color")
    @CommandPermission("foxtrot.team.management")
    @Description("Change the color of a system team")
    fun color(player: Player, team: Team) {
        if (team !is SystemTeam) throw ConditionFailedException("Team must be instance of SystemTeam to modify colors.")

        SelectColorMenu {
            team.color = ColorUtil.toChatColor(it)
            player.sendMessage("${CC.SEC}Team ${team.color}${team.name}${CC.SEC} now has a color of ${team.color}Example")
        }.openMenu(player)
    }

    @Subcommand("flag")
    @CommandPermission("foxtrot.team.management")
    @Description("Toggle a system flag on a team")
    fun flag(player: Player, team: Team, flag: SystemFlag) {
        if (team !is SystemTeam) throw ConditionFailedException("Team must be instance of SystemTeam to add flags.")
        if (team.hasFlag(flag)) team.flags.remove(flag)
        else team.flags.add(flag)

        player.sendMessage("${CC.SEC}Flag ${flag.name} set to ${TextUtil.stringifyBoolean(team.hasFlag(flag), TextUtil.FormatType.ENABLED_DISABLED)} ${CC.SEC}for team ${team.color}${team.name}")
    }

    @Subcommand("rename")
    @Description("Rename a team")
    fun rename(player: Player, @Conditions("validate:min=3,max=16,regex=[^a-zA-Z0-9]") name: String, @Default("self") team: Team) {
        if ((team is PlayerTeam && team.leader.uniqueId != player.uniqueId) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not the leader of ${team.name} so you cannot rename it.")

        team.name = name
        Bukkit.broadcastMessage("${Team.CHAT_PREFIX}${CC.PRI}${team.name}${CC.SEC} has been ${CC.GREEN}renamed${CC.SEC} to ${CC.PRI}$name${CC.SEC} by ${CC.PRI}${player.displayName}")
    }

    @Subcommand("debug-raw")
    @CommandPermission("foxtrot.team.development")
    @Description("Print debug info to chat")
    fun debug(player: Player, team: Team) {
        player.sendMessage(Serializers.gson.toJson(team))
    }

    @Subcommand("invite|inv")
    @Description("Invite a player to your team")
    fun invite(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.CREATE_INVITES)) throw ConditionFailedException("You are not allowed to invite members to ${team.name}.")

        if (team is PlayerTeam) {
            if (team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is already a member of your ${team.name}.")
            if (team.invites.contains(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} has already been invited to ${team.name}.")

            team.invites.add(target)
            team.broadcast("${CC.PRI}${player.name}${CC.SEC} has invited ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} to the team.")

            val targetPlayer = Bukkit.getPlayer(target) ?: return
            targetPlayer.sendMessage("${CC.SEC}You have been invited to join the team ${CC.PRI}${team.name}")
            FancyMessage().withMessage("${CC.PRI}Click here to join.").andCommandOf(ClickEvent.Action.RUN_COMMAND, "/team join ${team.name}").sendToPlayer(targetPlayer)
        } else throw ConditionFailedException("Retard.")
    }

    @Subcommand("uninvite")
    @Description("Revoke an invite for a player")
    fun revoke(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.REVOKE_INVITES)) throw ConditionFailedException("You are not revoke invites from players in ${team.name}.")

        if (team is PlayerTeam) {
            if (team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is already a member of your ${team.name}.")
            if (!team.invites.contains(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} has not been invited to ${team.name}.")

            team.invites.remove(target)
            team.broadcast("${CC.PRI}${player.name}${CC.SEC} has revoked an invitation for ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.")

        } else throw ConditionFailedException("Retard.")
    }

    @Subcommand("join")
    @Description("Join a team you were invited to")
    fun join(player: Player, team: Team) {
        if (TeamService.findTeamByPlayer(player.uniqueId) != null) throw ConditionFailedException("Leave your current team before attempting to join another team.")

        if (team is SystemTeam) throw ConditionFailedException("You cannot join teams that are managed by the server.")
        if (team is PlayerTeam)
        {
            if (!team.invites.contains(player.uniqueId)) throw ConditionFailedException("You have not been invited to join that team.")
            team.invites.remove(player.uniqueId)
            team.permissions[player.uniqueId] = mutableListOf()
            team.members.add(TeamMember(player.uniqueId, player.name, TeamMemberRole.MEMBER))
            team.broadcast("${CC.LIGHT_PURPLE}${player.name}${CC.YELLOW} has joined the team.")
            NametagHandler.reloadPlayer(player)
        }
    }

    @Subcommand("kick")
    @Description("Kick a member from your team")
    fun leave(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot kick players from system teams.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.KICK_MEMBER)) throw ConditionFailedException("You are not allowed kick members from ${team.name}.")

        if (team is PlayerTeam) {
            if (!team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not in your team ${team.name}.")
            if (team.raidable) throw ConditionFailedException("You cannot kick members from your team whilst you are raidable.")

            team.permissions.remove(target)
            team.members.removeIf {
                it.uniqueId == target
            }
            if (Bukkit.getPlayer(target) != null) Bukkit.getPlayer(target).sendMessage("${CC.RED}You have been kicked from ${team.name}.")
            team.broadcast("${CC.LIGHT_PURPLE}${player.name}${CC.YELLOW} has kicked ${CC.LIGHT_PURPLE}${ScalaStoreUuidCache.username(target)}${CC.SEC} from the team.")
        }
    }

    @Subcommand("leave")
    @Description("Leave your current team")
    fun leave(player: Player, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot leave system teams.")
        if (team is PlayerTeam)
        {
            if (team.leader.uniqueId == player.uniqueId) throw ConditionFailedException("You cannot leave your team as you are the leader. Please try using /team disband instead.")
            val teamAt = LandBoard.getTeam(player.location)
            if (!team.isMember(player.uniqueId)) throw ConditionFailedException("You are not in the team ${team.name}.")
            if (teamAt != null && teamAt == team) throw ConditionFailedException("You cannot leave a team whilst you remain on their territory.")
            if (team.raidable) throw ConditionFailedException("You cannot leave your team whilst you are raidable.")
            if (team.regenTime > System.currentTimeMillis()) throw ConditionFailedException("You cannot leave your team whilst you are regenerating dtr.")

            team.permissions.remove(player.uniqueId)
            team.members.removeIf {
                it.uniqueId == player.uniqueId
            }
            player.sendMessage("${CC.RED}You have left ${team.name}.")
            team.broadcast("${CC.LIGHT_PURPLE}${player.name}${CC.YELLOW} has left the team.")
        }
    }

    @Subcommand("show|info|i|who")
    @Description("Show information on a team")
    fun show(player: Player, @Default("self") team: Team) {
        player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(52)}")
        if (team is PlayerTeam) {
            player.sendMessage("${team.getName(player)} ${CC.GRAY}- ${CC.SEC}Members: ${CC.PRI}${team.onlineMemberCount}/${team.size}")
            player.sendMessage("")
            player.sendMessage("${CC.SEC}Leader: ${CC.WHITE}${(if (team.offlineMembers.contains(team.leader.uniqueId)) CC.GRAY else CC.GREEN) + team.leader.name}")
            if (team.leaders.isNotEmpty()) player.sendMessage("${CC.SEC}Co-Leaders: ${CC.WHITE}${
                team.leaders.joinToString(
                    ","
                ) { (if (team.offlineMembers.contains(it.uniqueId)) CC.GRAY else CC.GREEN) + it.name }
            }")
            if (team.officers.isNotEmpty()) player.sendMessage("${CC.SEC}Officers: ${CC.WHITE}${
                team.officers.joinToString(
                    ","
                ) { (if (team.offlineMembers.contains(it.uniqueId)) CC.GRAY else CC.GREEN) + it.name }
            }")
            if (team.members.any { it.role == TeamMemberRole.MEMBER }) player.sendMessage("${CC.SEC}Members: ${CC.WHITE}${
                team.members.filter { it.role == TeamMemberRole.MEMBER }.joinToString(
                    ","
                ) { (if (team.offlineMembers.contains(it.uniqueId)) CC.GRAY else CC.GREEN) + it.name }
            }")

            if (team.isMember(player.uniqueId))
            {
                if (team.announcement != null) player.sendMessage("${CC.SEC}Announcement: ${CC.PRI}${team.announcement}")
                if (team.discord != null) player.sendMessage("${CC.SEC}Discord: ${CC.PRI}${team.discord}")
                player.sendMessage("${CC.SEC}Balance: ${CC.GREEN}$${team.balance} ${CC.GRAY}- ${CC.SEC}Kills: ${CC.RED}0 ${CC.GRAY}- ${CC.SEC}Lives: ${CC.LIGHT_PURPLE}0")
            }
            player.sendMessage("${CC.SEC}Founded on: ${CC.WHITE}${TimeUtil.formatIntoFullCalendarString(Date(team.foundedOn))}")
            player.sendMessage(("${CC.SEC}Deaths until Raidable: ${CC.RED}${team.getDTRFormatted()}"))
            if (team.regenTime > System.currentTimeMillis()) {
                player.sendMessage("${CC.SEC}Regeneration: ${CC.PRI}${TimeUtil.formatIntoDetailedString(((team.regenTime - System.currentTimeMillis()) / 1000).toInt())}")
            }

            player.sendMessage("${CC.SEC}Raidable: ${TextUtil.stringifyBoolean(team.raidable, TextUtil.FormatType.YES_NO)}")
            if (team.home == null) player.sendMessage("${CC.SEC}Home: ${CC.PRI}Not Set")
            else player.sendMessage("${CC.SEC}Home: ${CC.PRI}${team.home!!.blockX}, ${team.home!!.blockZ}")
        } else {
            player.sendMessage(team.getName(player))
            player.sendMessage("")
        }
        if (team is SystemTeam) {
            if (team.claims.isNotEmpty()) {
                val claim = team.claims.first()
                player.sendMessage("${CC.SEC}Location: ${CC.PRI}${claim.x3}, ${claim.z3}")
            }
            if (team.hasFlag(SystemFlag.SAFE_ZONE)) {
                player.sendMessage("${CC.GRAY}This area is a safezone. This means you are protected from all forms of damage.")
            }
        }
        if (team.ownsLocation(player.location)) {
            player.sendMessage("${CC.SEC}You are currently in this teams territory.")
            if (team is PlayerTeam && TeamService.findTeamByPlayer(player.uniqueId) != null) {
                FancyMessage().withMessage("${CC.SEC}Click here to focus this team.").andCommandOf(ClickEvent.Action.RUN_COMMAND, "/team focus ${team.name}").sendToPlayer(player)
            }
        }
        player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(52)}")
    }

    @Subcommand("disband")
    @Description("Disband your current team")
    fun disband(player: Player, @Default("self") team: Team) {
        if ((team is PlayerTeam && team.leader.uniqueId != player.uniqueId) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not the leader of ${team.name} so you cannot disband it.")

        TeamService.unregisterTeam(team)
        Bukkit.broadcastMessage("${Team.CHAT_PREFIX}${CC.PRI}${team.name}${CC.SEC} has been ${CC.RED}disbanded${CC.SEC} by ${CC.PRI}${player.displayName}")
    }

    @Subcommand("online|onlinecount|onlinemembers")
    @Description("View online players")
    fun online(player: Player, @Default("self") team: Team) {
        if (team is PlayerTeam) player.sendMessage("${Team.CHAT_PREFIX}${CC.SEC}There is ${CC.PRI}${team.onlineMemberCount}${CC.SEC} online members in ${team.getName(player)}${CC.SEC}.")
        else throw ConditionFailedException("There is no members in a system team.")
    }

    @Subcommand("list|l")
    @Description("View a list of online teams")
    fun list(player: Player, @Default("1") page: Int) {
        TeamListPaginatedResult.display(player, TeamService.teams.filterIsInstance<PlayerTeam>().filter {
            it.onlineMemberCount > 0
        }, page, "team list %s")
    }

    @Subcommand("announcement|message|msg|alert|")
    @Description("Update your teams announcement message")
    fun announcement(player: Player, announcement: String, @Default("self") team: Team) {
        if ((team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.UPDATE_ANNOUNCEMENT)) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not a member of ${team.name} so you cannot deposit to it.")
        if (team is SystemTeam) throw ConditionFailedException("You cannot set the announcement of a system team.")

        if (team is PlayerTeam) {
            team.broadcast("${CC.SEC}${player.name} has updated the team announcement to ${CC.PRI}${announcement}${CC.SEC}.")
            team.announcement = announcement
        }
    }

    @Subcommand("deposit|d|addmoney")
    @Description("Deposit money into your team")
    fun deposit(player: Player, input: String, @Default("self") team: Team) {
        if ((team is PlayerTeam && team.isMember(player.uniqueId)) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not a member of ${team.name} so you cannot deposit to it.")
        if (team is SystemTeam) throw ConditionFailedException("You cannot deposit money into a system team.")

        val amount = if (input.equals("all", true)) BalancePersistMap[player.uniqueId] ?: 0.0
        else if (input.toDoubleOrNull() == null) throw ConditionFailedException("Amount must be a positive integer.")
        else input.toDouble()
        if ((BalancePersistMap[player.uniqueId] ?: 0.0) < amount) throw ConditionFailedException("You cannot afford to deposit $${amount} into your teams balance.")

        if (team is PlayerTeam) {
            team.broadcast("${CC.SEC}${player.name} has deposited ${CC.PRI}$${amount}${CC.SEC} into the team balance.")
            team.balance += amount
            BalancePersistMap.minus(player.uniqueId, amount)
        }
    }

    @Subcommand("withdraw|w|takemoney")
    @Description("Withdraw money from your team")
    fun withdraw(player: Player, amount: Double, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot withdraw money from a system team.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.WITHDRAW_BALANCE)) throw ConditionFailedException("You are not allowed to withdraw money from ${team.name}.")


        if (team is PlayerTeam) {
            if (team.balance < amount) throw ConditionFailedException("You cannot afford to withdraw $${amount} from your teams balance.")

            team.broadcast("${CC.SEC}${player.name} has withdrawn ${CC.PRI}$${amount}${CC.SEC} from the team balance.")
            team.balance -= amount
            BalancePersistMap.plus(player.uniqueId, amount)
        }
    }

    @Subcommand("officer add")
    @Description("Promote a team member")
    fun addOfficer(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot promote members in system team.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.PROMOTE_OFFICER)) throw ConditionFailedException("You are not allowed to promote members in ${team.name}.")

        if (team is PlayerTeam) {
            if (!team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not a member of your team.")
            if (team.isCoLeader(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is already a co-leader in your team.")
            if (team.isCaptain(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is already an officer in your team.")

            team.getMember(target)!!.role = TeamMemberRole.OFFICER
            team.broadcast("${CC.SEC}${player.name} has promoted ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} to officer.")
        }
    }

    @Subcommand("officer remove")
    @Description("Demote a team member")
    fun removeOfficer(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot promote members in system team.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.DEMOTE_OFFICER)) throw ConditionFailedException("You are not allowed to promote members in ${team.name}.")

        if (team is PlayerTeam) {
            if (!team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not a member of your team.")
            if (!team.isCaptain(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not an officer in your team.")

            team.getMember(target)!!.role = TeamMemberRole.MEMBER
            team.broadcast("${CC.SEC}${player.name} has demoted ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} to member.")
        }
    }

    @Subcommand("coleader add")
    @Description("Promote a team member")
    fun addCoLeader(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot promote members in system team.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.PROMOTE_CO_LEADER)) throw ConditionFailedException("You are not allowed to promote members in ${team.name}.")

        if (team is PlayerTeam) {
            if (!team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not a member of your team.")
            if (team.isCoLeader(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is already a co-leader in your team.")

            team.getMember(target)!!.role = TeamMemberRole.CO_LEADER
            team.broadcast("${CC.SEC}${player.name} has promoted ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} to co-leader.")
        }
    }

    @Subcommand("coleader remove")
    @Description("Demote a team member")
    fun removeCoLeader(player: Player, target: UUID, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot promote members in system team.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.DEMOTE_CO_LEADER)) throw ConditionFailedException("You are not allowed to promote members in ${team.name}.")

        if (team is PlayerTeam) {
            if (!team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not a member of your team.")
            if (!team.isCoLeader(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is not an co-leader in your team.")

            team.getMember(target)!!.role = TeamMemberRole.MEMBER
            team.broadcast("${CC.SEC}${player.name} has demoted ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} to member.")
        }
    }

    @Subcommand("map")
    @Description("Display the team visual map")
    fun map(player: Player) {
        VisualClaim(player, VisualClaimType.MAP, false).draw(false)
    }

    @Subcommand("chat|c|channel|joinchannel")
    @Description("Change your team chat channel")
    fun channel(player: Player, channel: ChatMode) {
        ChatModePersistMap[player.uniqueId] = channel
        player.sendMessage("${CC.SEC}You are now in ${CC.PRI}${channel.name}${CC.SEC} chat.")
    }

    @Subcommand("focus|target")
    fun focus(player: Player, target: Team) {
        if (target is SystemTeam) throw ConditionFailedException("You cannot focus a system team.")
        val team = TeamService.findTeamByPlayer(player.uniqueId) ?: throw ConditionFailedException("You cannot focus players whilst you are not in a team.")
        if (team == target) throw ConditionFailedException("You cannot focus your own team.")

        team.focused = target.identifier
        team.broadcast("${CC.SEC}Your team is now focused on the team ${CC.PRI}${target.name}${CC.SEC}.")
    }

    @Subcommand("home|hq|go")
    @Description("Teleport to your team home location")
    fun home(player: Player, @Default("self") team: Team) {
        if (team !is PlayerTeam) throw ConditionFailedException("You cannot teleport to system teams.")
        if (!team.isMember(player.uniqueId)) throw ConditionFailedException("You cannot teleport to teams who you are not a part of.")
        if (team.home == null) throw ConditionFailedException("Your team does not have a home location set. Set one using /team sethome inside of your claimed land.")

        TeamHomeMap.startCooldown(player.uniqueId)
    }

    @Subcommand("sethome|sethq")
    @Description("Update your teams home location")
    fun setHome(player: Player, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot set the location of system teams.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.UPDATE_HOME)) throw ConditionFailedException("You are not allowed to update the home location of ${team.name}.")
        if (LandBoard.getTeam(player.location) != team) throw ConditionFailedException("Your team does not own this land.")

        if (team is PlayerTeam) {
            team.home = player.location
            team.broadcast("${CC.LIGHT_PURPLE}${player.name} ${CC.YELLOW} has updated the team home location.")
        }
    }

    @Subcommand("stuck")
    @Description("Teleport to a safe location")
    fun stuck(player: Player) {
        if (SystemFlag.SAFE_ZONE.appliesAt(player.location)) {
            player.teleport(Bukkit.getWorld("world").spawnLocation)
            return
        }

        TeamStuckMap.startCooldown(player.uniqueId)
    }

    @Subcommand("permissions|perms|manageperms")
    @Description("Manage your team members permissions")
    fun permissions(player: Player, @Default("self") team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot modify permissions of system teams.")
        if (team is PlayerTeam && !team.isOwner(player.uniqueId)) throw ConditionFailedException("You do not have permission to modify the permissions of ${team.name}.")

        TeamPermissionsMenu(team as PlayerTeam).openMenu(player)
    }

    @Subcommand("claim")
    @Description("Obtain a claiming wand to create land")
    fun claim(player: Player, @Default("self") team: Team) {
        //TOO: ADD kitmap check
        if (team !is SystemTeam && MapService.isWarzone(player.location)) throw ConditionFailedException("You are currently in the Warzone and can't claim land here. The Warzone ends at ${MapService.WARZONE_RADIUS}.")
        if (team is SystemTeam && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not allowed to claim land for system teams.")
        if (team is PlayerTeam && !team.hasPermission(player.uniqueId, TeamMemberPermission.CLAIM_LAND)) throw ConditionFailedException("You are not allowed to claim land for ${team.name}.")

        player.inventory.remove(Team.SELECTION_WAND)
        if (team is PlayerTeam && team.raidable) throw ConditionFailedException("You may not claim land while your team is raidable.")
        var slot = -1
        for (i in 0..8)
        {
            if (player.inventory.getItem(i) == null) {
                slot = i
                break
            }
        }

        if (slot == -1) throw ConditionFailedException("You don't have space in your hotbar for the claim wand.")

        Tasks.asyncDelayed(1L) { player.inventory.setItem(slot, Team.SELECTION_WAND.clone())}

        VisualClaim(player, VisualClaimType.CREATE, team is SystemTeam).draw(false)
        if (!VisualClaim.currentMaps.containsKey(player.name)) VisualClaim(player, VisualClaimType.MAP, team is SystemTeam).draw(true)
        player.sendMessage("${CC.SEC}Gave you a claim wand.")
        if (team is SystemTeam) {
            ClaimService[player.uniqueId] = team
            player.sendMessage("${CC.RED}WARNING! YOUR LAND WILL BE CLAIMED FOR THE SYSTEM TEAM ${team.name}")
        }

    }

}