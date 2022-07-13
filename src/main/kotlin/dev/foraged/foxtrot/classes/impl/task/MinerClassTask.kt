package dev.foraged.foxtrot.classes.impl.task

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.classes.impl.MinerClass
import me.lucko.helper.promise.ThreadContext
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.potion.PotionEffectType

@Repeating(20L, 20L, ThreadContext.SYNC)
class MinerClassTask : Runnable
{
    override fun run()
    {
        MinerClass.noDamage.keys.forEach {
            val left = MinerClass.noDamage.remove(it)

            val player = Bukkit.getPlayerExact(it) ?: return
            if (left == 0 || left == null) {
                if (player.location.y <= MinerClass.Y_HEIGHT) {
                    MinerClass.invis[it] = 10
                    player.sendMessage("${CC.BLUE}Miner Invisibility ${CC.YELLOW}will be activated in 10 seconds.")
                }
            } else MinerClass.noDamage[it] = left - 1
        }

        MinerClass.invis.entries.forEach {
            val player = Bukkit.getPlayerExact(it.key) ?: return

            val secs = it.value
            if (secs == 0) {
                if (player.location.y <= MinerClass.Y_HEIGHT) {
                    if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        player.sendMessage("${CC.BLUE}Miner Invisibility${CC.YELLOW} has been enabled!")
                        player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Int.MAX_VALUE, 0))
                    }
                }
            } else MinerClass.invis[it.key] = secs -1
        }
    }
}