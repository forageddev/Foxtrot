package dev.foraged.foxtrot.enchant

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

abstract class Enchant(
    val id: String,
    val name: String,
    val color: ChatColor,
    val maxLevel: Int
) : Listener
{
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[Enchantments] "
    }

    val displayName: String get() = "$color${CC.BOLD}$name"
    val icon: ItemStack get() = ItemBuilder.of(XMaterial.FIREWORK_STAR).name(displayName).setFireworkEffect(fireworkEffect).build()

    internal var fireworkEffect = when (color) {
        ChatColor.RED -> FireworkEffect.builder().withColor(Color.RED).withFade(Color.RED).build()
        ChatColor.WHITE -> FireworkEffect.builder().withColor(DyeColor.WHITE.color).build()
        ChatColor.GOLD -> FireworkEffect.builder().withColor(Color.ORANGE).withFade(Color.ORANGE).build()
        ChatColor.GREEN -> FireworkEffect.builder().withColor(Color.fromBGR(0,255,0)).build()
        ChatColor.YELLOW -> FireworkEffect.builder().withColor(Color.YELLOW).build()
        else -> FireworkEffect.builder().withColor(Color.WHITE).withFade(Color.WHITE).build()
    }

    fun getFormattedLoreName(): String {
        return "$color${Constants.DOUBLE_ARROW_RIGHT} ${color}${CC.BOLD}${name}${CC.WHITE}"
    }

    abstract fun canEnchant(item: ItemStack) : Boolean
    open fun tick(player: Player, level: Int) { }

    fun buildEnchantStar(level: Int): ItemStack {
        return ItemBuilder.of(XMaterial.FIREWORK_STAR)
            .name("$color${CC.BOLD}Enchantment Star")
            .setLore(listOf(buildString {
                append(getFormattedLoreName())
                append(" ")

                append(level.toString())
            }))
            .addFlags(ItemFlag.HIDE_POTION_EFFECTS) // hide firework info coz psigot is werid -foraged
            .glow()
            .build()
    }

    fun isEnchantStar(item: ItemStack) : Boolean {
        return getStarLevel(item) != -1
    }

    fun getStarLevel(item: ItemStack): Int {
        if (item.type != Material.FIREWORK_CHARGE) return -1
        if (!item.itemMeta.hasDisplayName() || !item.itemMeta.displayName.contains("Enchantment Star")) return -1
        if (!item.itemMeta.hasLore() || item.itemMeta.lore.size <= 0) return -1
        if (!item.itemMeta.lore[0].contains(getFormattedLoreName())) return -1
        val lore = item.itemMeta.lore[0].split(" ").toTypedArray()
        return try {
            Integer.valueOf(lore[lore.size - 1])
        } catch (e: Exception) {
            -1
        }
    }

    fun hasEnchant(item: ItemStack) : Boolean {
        return EnchantService.findEnchants(item).containsKey(this)
    }

    fun getEnchantLevel(item: ItemStack) : Int {
        return EnchantService.findEnchants(item)[this] ?: -1
    }

    fun apply(item: ItemStack, addLevel: Int) {
        val meta = item.itemMeta

        var level = getEnchantLevel(item)
        if (level == -1) level = 0

        val lore = meta.lore ?: mutableListOf()
        val string = getFormattedLoreName() + " " +  (level + addLevel)
        var updated = false
        lore.forEachIndexed { index, s ->
            if (s.startsWith(getFormattedLoreName())) {
                updated = true
                lore[index] = string
            }
        }
        if (!updated) lore.add(string)
        meta.lore = lore
        item.itemMeta = meta
    }
}