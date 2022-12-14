package dev.foraged.foxtrot.shop

import com.cryptomorin.xseries.XMaterial
import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.listener.CrowbarListener
import dev.foraged.foxtrot.map.BalancePersistMap
import dev.foraged.foxtrot.team.enums.SystemFlag
import gg.scala.flavor.service.Service
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.floor

@Service
@Listeners
object ShopService : Listener
{
    fun findShopByBlock(block: Block) : Shop? {
        if (block.state is Sign) {
            val sign = block.state as Sign
            val lines = sign.lines

            val type = try
            {
                ShopType.valueOf(
                    ChatColor.stripColor(
                        lines[0]
                            .replace("-", "")
                            .replace(" ", "")
                    ).uppercase()
                )
            } catch (e: IllegalArgumentException) {
                null
            }  ?: return null

            val material = lines[1]

            var item = try
            {
                ItemBuilder.of(
                    when (material)
                    {
                        "Crowbar" -> XMaterial.IRON_AXE
                        else -> XMaterial.valueOf(material.uppercase())
                    }
                )
            } catch (ex: IllegalArgumentException) {
                null
            }

            if (item != null && material == "Crowbar") {
                item.name(CrowbarListener.CROWBAR_NAME)
                item.setLore(CrowbarListener.getCrowbarDescription(6, 1))
            }
            if (material.contains("Spawner")) {
                item = ItemBuilder.of(XMaterial.SPAWNER).name(material)
            }
            if (material.contains("Egg")) {
                item = ItemBuilder.of(Material.MONSTER_EGG).data(EntityType.valueOf(material.replace(" ", "_").uppercase().replace("_EGG", "")).typeId)
            }

            if (item == null) return null
            return Shop(sign, type, material, item.build(), lines[2].toInt(), lines[3].replace("$", "").toDouble())
        }
        return null
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock
            if (!SystemFlag.SAFE_ZONE.appliesAt(block.location)) return
            val shop = findShopByBlock(block) ?: return

            event.isCancelled = true
            Tasks.delayed(30L) {
                if (player.world == block.world && player.location.distance(block.location) < 25) {
                    player.sendSignChange(block.location, shop.sign.lines)
                }
            }

            when (shop.type) {
                ShopType.BUY -> {
                    if (player.inventory.firstEmpty() == -1) {
                        player.sendMessage("${CC.RED}You cannot purchase anything whilst your inventory is full.")
                        player.sendSignChange(block.location, arrayOf("${CC.RED}No space for", shop.sign.lines[1], shop.sign.lines[2], shop.sign.lines[3]))
                        return
                    }

                    if ((BalancePersistMap[player.uniqueId] ?: 0.0) < shop.price) {
                        player.sendMessage("${CC.RED}You cannot afford to purchase this item.")
                        player.sendSignChange(block.location, arrayOf("${CC.RED}Cannot afford", shop.sign.lines[1], shop.sign.lines[2], shop.sign.lines[3]))
                        return
                    }

                    BalancePersistMap.minus(player.uniqueId, shop.price)
                    player.inventory.addItem(ItemBuilder.copyOf(shop.item).amount(shop.amount).build())
                    player.sendSignChange(block.location, arrayOf("${CC.GREEN}Purchased", shop.sign.lines[1], shop.sign.lines[2], shop.sign.lines[3]))
                    player.updateInventory()
                    player.sendMessage("${CC.GREEN}You have purchased ${CC.BOLD}x${shop.amount} ${CC.GREEN}${shop.itemName} from the shop for ${CC.BOLD}$${shop.price}${CC.GREEN}.")
                }
                ShopType.SELL -> {
                    if (!player.inventory.contains(shop.item.type)) {
                        player.sendMessage("${CC.RED}You do not have enough items to sell.")
                        player.sendSignChange(block.location, arrayOf("${CC.RED}Not carrying", shop.sign.lines[1], "${CC.RED}on you", shop.sign.lines[3]))
                        return
                    }

                    val pricePer = shop.price / shop.amount
                    var sellCount = 0
                    for (item in player.inventory.contents.filterNotNull()) {
                        if (!item.isSimilar(shop.item)) continue
                        if (sellCount >= shop.amount && !player.isSneaking) break
                        if (sellCount + item.amount >= shop.amount && !player.isSneaking) {
                            sellCount = shop.amount
                            break
                        }
                        sellCount += item.amount
                    }

                    val amount = floor(pricePer * sellCount)
                    BalancePersistMap.plus(player.uniqueId, amount)
                    player.sendMessage("${CC.GREEN}You have sold ${CC.BOLD}x${sellCount} ${CC.GREEN}${shop.itemName} to the shop for ${CC.BOLD}$${amount}${CC.GREEN}.")
                    player.inventory.removeItem(ItemBuilder.copyOf(shop.item).amount(sellCount).build())
                    player.sendSignChange(block.location, arrayOf("${CC.GREEN}Sold", shop.sign.lines[1], shop.sign.lines[2], shop.sign.lines[3]))
                }
            }
        }
    }
}