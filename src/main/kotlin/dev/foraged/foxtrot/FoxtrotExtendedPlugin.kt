package dev.foraged.foxtrot

import dev.foraged.commons.ExtendedPaperPlugin
import dev.foraged.commons.annotations.container.ContainerEnable
import dev.foraged.commons.annotations.container.flavor.LazyStartup
import dev.foraged.commons.persist.impl.IntegerPersistMap
import dev.foraged.foxtrot.map.cooldown.OpplePersistableMap
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.map.cooldown.nopersist.EnderpearlMap
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.ArcherJumpMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.ArcherSpeedMap
import dev.foraged.foxtrot.map.ore.impl.*
import dev.foraged.foxtrot.map.stats.*
import dev.foraged.foxtrot.ui.FoxtrotNametagProvider
import dev.foraged.foxtrot.ui.FoxtrotScoreboardProvider
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.scoreboard.ScoreboardHandler

@Plugin(
    name = "Foxtrot",
    version = "\${git.commit.id.abbrev}",
    authors = ["Foraged"],
    depends = [
        PluginDependency("LunarClient-API"),
        PluginDependency("Commons")
    ]
)
@LazyStartup
class FoxtrotExtendedPlugin : ExtendedPaperPlugin()
{
    companion object {
        @JvmStatic lateinit var instance: FoxtrotExtendedPlugin

        val statMaps = listOf<IntegerPersistMap>(
            KillsPersistMap,
            DeathsPersistMap,
            KillstreakPersistMap,
            MaxKillstreakPersistMap,
            PotionsDrankPersistMap,
            PotionsSplashedPersistMap,
            BlocksMinedPersistMap,
            BlocksPlacedPersistMap
        )

        val oreMaps = listOf(
            CoalPersistableMap,
            DiamondPersistableMap,
            EmeraldPersistableMap,
            GoldPersistableMap,
            IronPersistableMap,
            LapisPersistableMap,
            QuartzPersistableMap,
            RedstonePersistableMap
        )

        val timerMaps = listOf(
            EnderpearlMap,
            SpawnTagMap,
            ArcherJumpMap,
            ArcherSpeedMap,
        )

        val persistableTimerMaps = listOf(
            PvPTimerPersistableMap,
            OpplePersistableMap
        )
    }


    @ContainerEnable
    fun open() {
        instance = this

        NametagHandler.registerProvider(FoxtrotNametagProvider)
        ScoreboardHandler.configure(FoxtrotScoreboardProvider)
    }
}