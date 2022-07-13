package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.event.team.TeamCreateEvent
import dev.foraged.foxtrot.map.BalancePersistMap
import dev.foraged.foxtrot.server.ServerHandler
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamHandler
import dev.foraged.foxtrot.team.claim.ClaimHandler
import dev.foraged.foxtrot.team.claim.VisualClaim
import dev.foraged.foxtrot.team.claim.VisualClaimType
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberRole
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.menu.menus.SelectColorMenu
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.text.TextUtil
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
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
            if (arg.equals("self", ignoreCase = true)) return@registerContext TeamHandler.findTeamByPlayer((it.sender as Player).uniqueId)
                ?: throw ConditionFailedException("You are not currently in a team. Please create a new team using /team create <name>")
            return@registerContext TeamHandler.findTeamByName(arg) ?: throw ConditionFailedException("Team with name or member $arg not found.")
        }
    }

    @Subcommand("create")
    @Description("Create a new team")
    fun create(player: Player, @Single @Conditions("validate:min=3,max=16,regex=[^a-zA-Z0-9]") name: String) {
        if (TeamHandler.findTeamByName(name) != null) throw ConditionFailedException("Another team with the name $name already exists. Please choose a new name for your team.")
        if (TeamHandler.findTeamByPlayer(player.uniqueId) != null) throw ConditionFailedException("You are already in another team. Please leave your existing team before attempting to perform this command again.")

        val team = PlayerTeam(UUID.randomUUID(), name, TeamMember(player.uniqueId, player.name, TeamMemberRole.LEADER))
        if (!TeamCreateEvent(team).call()) throw ConditionFailedException("Your team creation was cancelled.")

        TeamHandler.registerTeam(team)
        Bukkit.broadcastMessage("${Team.CHAT_PREFIX}${CC.PRI}$name${CC.SEC} has been ${CC.GREEN}opened${CC.SEC} by ${CC.PRI}${player.displayName}")
    }

    @Subcommand("createsystem")
    @CommandPermission("foxtrot.team.management")
    @Description("Create a new system team")
    fun createSystem(player: Player, name: String) {
        TeamHandler.registerTeam(SystemTeam(UUID.randomUUID(), name))
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

    @Subcommand("invite")
    @Description("Invite a player to your team")
    fun invite(player: Player, target: UUID, @Default("self") team: Team) {
        if (((team is PlayerTeam) && (team.isOwner(player.uniqueId) || team.isCaptain(player.uniqueId) || team.isCoLeader(player.uniqueId))) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not an officer of ${team.name} so you cannot invite members to it.")
        if (team is PlayerTeam) {
            if (team.isMember(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} is already a member of your ${team.name}.")
            if (team.invites.contains(target)) throw ConditionFailedException("${ScalaStoreUuidCache.username(target)} has already been invited to ${team.name}.")

            team.invites.add(target)
            team.broadcast("${CC.PRI}${player.name}${CC.SEC} has invited ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} to the team!")

            val targetPlayer = Bukkit.getPlayer(target) ?: return
            targetPlayer.sendMessage("${CC.SEC}You have been invited to join the team ${CC.PRI}${team.name}")
            FancyMessage().withMessage("${CC.PRI}Click here to join.").andCommandOf(ClickEvent.Action.RUN_COMMAND, "team join ${team.name}").sendToPlayer(targetPlayer)
        } else throw ConditionFailedException("Retard.")
    }

    @Subcommand("join")
    @Description("Join a team you were invited to")
    fun join(player: Player, team: Team) {
        if (team is SystemTeam) throw ConditionFailedException("You cannot join teams that are managed by the server.")
        if (team is PlayerTeam)
        {
            if (!team.invites.contains(player.uniqueId)) throw ConditionFailedException("You have not been invited to join that team.")
            team.invites.remove(player.uniqueId)
            team.members.add(TeamMember(player.uniqueId, player.name, TeamMemberRole.MEMBER))
            team.broadcast("${CC.LIGHT_PURPLE}${player.name}${CC.YELLOW} has joined the team.")
        }
    }

    @Subcommand("show|info|i|who")
    @Description("Show information on a team")
    fun show(player: Player, @Default("self") team: Team) {
        player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(52)}")
        player.sendMessage(team.getName(player))
        player.sendMessage("")
        if (team is PlayerTeam)
        {
            player.sendMessage("${CC.YELLOW}Leader: ${CC.WHITE}${team.leader.name}")
            if (team.leaders.isNotEmpty()) player.sendMessage("${CC.YELLOW}Co-Leaders: ${CC.WHITE}${
                team.leaders.joinToString(
                    ","
                ) { (if (team.offlineMembers.contains(it.uniqueId)) CC.GRAY else CC.GREEN) + it.name }
            }")
            if (team.officers.isNotEmpty()) player.sendMessage("${CC.YELLOW}Officers: ${CC.WHITE}${
                team.officers.joinToString(
                    ","
                ) { (if (team.offlineMembers.contains(it.uniqueId)) CC.GRAY else CC.GREEN) + it.name }
            }")
            if (team.members.filter { it.role == TeamMemberRole.MEMBER }.isNotEmpty()) player.sendMessage("${CC.YELLOW}Members: ${CC.WHITE}${
                team.members.filter { it.role == TeamMemberRole.MEMBER }.joinToString(
                    ","
                ) { (if (team.offlineMembers.contains(it.uniqueId)) CC.GRAY else CC.GREEN) + it.name }
            }")

            if (team.isMember(player.uniqueId)) {
                player.sendMessage("${CC.YELLOW}Balance: ${CC.BLUE}$${team.balance}")
            }
        }
        player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(52)}")
    }

    @Subcommand("disband")
    @Description("Disband your current team")
    fun disband(player: Player, @Default("self") team: Team) {
        if ((team is PlayerTeam && team.leader.uniqueId != player.uniqueId) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not the leader of ${team.name} so you cannot disband it.")

        TeamHandler.unregisterTeam(team)
        Bukkit.broadcastMessage("${Team.CHAT_PREFIX}${CC.PRI}${team.name}${CC.SEC} has been ${CC.RED}disbanded${CC.SEC} by ${CC.PRI}${player.displayName}")
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
            team.broadcast("${CC.SEC}${player.name} has deposited ${CC.BLUE}$${amount}${CC.SEC} into the team balance.")
            team.balance += amount
        }
    }

    @Subcommand("map")
    @Description("Display the team visual map")
    fun map(player: Player) {
        VisualClaim(player, VisualClaimType.MAP, false).draw(false)
    }

    @Subcommand("claim")
    @Description("Obtain a claiming wand to create land")
    fun claim(player: Player, @Default("self") team: Team) {
        //TOO: ADD kitmap check
        if (team !is SystemTeam && player.gameMode == GameMode.CREATIVE && ServerHandler.isWarzone(player.location)) throw ConditionFailedException("You are currently in the Warzone and can't claim land here. The Warzone ends at \" + ServerHandler.WARZONE_RADIUS + \".\"")
        if (!(team is SystemTeam || ((team is PlayerTeam) && (team.isOwner(player.uniqueId) || team.isCaptain(player.uniqueId) || team.isCoLeader(player.uniqueId)))) && !player.hasPermission("foxtrot.team.management")) throw ConditionFailedException("You are not an officer of ${team.name} so you cannot claim for it.")

        player.inventory.remove(Team.SELECTION_WAND)
        if (team is PlayerTeam && team.raidable) throw ConditionFailedException("You may not claim land while your faction is raidable!")
        var slot = -1
        for (i in 0..8)
        {
            if (player.inventory.getItem(i) == null) {
                slot = i
                break
            }
        }

        if (slot == -1) throw ConditionFailedException("You don't have space in your hotbar for the claim wand!")

        Tasks.asyncDelayed(1L) { player.inventory.setItem(slot, Team.SELECTION_WAND.clone())}

        VisualClaim(player, VisualClaimType.CREATE, team is SystemTeam).draw(false)
        if (!VisualClaim.currentMaps.containsKey(player.name)) VisualClaim(player, VisualClaimType.MAP, team is SystemTeam).draw(true)
        player.sendMessage("${CC.SEC}Gave you a claim wand.")
        if (team is SystemTeam) {
            ClaimHandler[player.uniqueId] = team
            player.sendMessage("${CC.RED}WARNING! YOUR LAND WILL BE CLAIMED FOR THE SYSTEM TEAM ${team.name}")
        }

    }

}