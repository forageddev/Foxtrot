package dev.foraged.foxtrot.shop

import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

data class Shop(val sign: Sign, val type: ShopType, val item: ItemStack, val amount: Int, val price: Double)