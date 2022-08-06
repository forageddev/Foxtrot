package dev.foraged.foxtrot.team.claim

import dev.foraged.foxtrot.team.Team
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import kotlin.math.abs

class Claim : Iterable<Coordinate?>
{
    var world: String

    var x1: Int
    var y1: Int
    var z1: Int
    var x2: Int
    var y2: Int
    var z2: Int
    var name: String? = null

    constructor(corner1: Location, corner2: Location) : this(
        corner1.world.name,
        corner1.blockX,
        corner1.blockY,
        corner1.blockZ,
        corner2.blockX,
        corner2.blockY,
        corner2.blockZ
    )

    val x3: Int get() = (x1 + x2) / 2
    val z3: Int get() = (z1 + z2) / 2

    constructor(copyFrom: Claim)
    {
        world = copyFrom.world
        x1 = copyFrom.x1
        y1 = copyFrom.y1
        z1 = copyFrom.z1
        x2 = copyFrom.x2
        y2 = copyFrom.y2
        z2 = copyFrom.z2
        name = copyFrom.name
    }

    constructor(world: String, x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int)
    {
        this.world = world
        this.x1 = x1.coerceAtMost(x2)
        this.x2 = x1.coerceAtLeast(x2)
        this.y1 = y1.coerceAtMost(y2)
        this.y2 = y1.coerceAtLeast(y2)
        this.z1 = z1.coerceAtMost(z2)
        this.z2 = z1.coerceAtLeast(z2)
    }

    override fun equals(`object`: Any?): Boolean
    {
        if (`object` !is Claim)
        {
            return false
        }
        val claim = `object`
        return claim.maximumPoint == maximumPoint && claim.minimumPoint == minimumPoint
    }

    val minimumPoint: Location
        get() = Location(
            Bukkit.getServer().getWorld(world), x1.coerceAtMost(x2)
                .toDouble(), y1.coerceAtMost(y2).toDouble(), z1.coerceAtMost(z2).toDouble()
        )
    val maximumPoint: Location
        get() = Location(
            Bukkit.getServer().getWorld(world), x1.coerceAtLeast(x2)
                .toDouble(), y1.coerceAtLeast(y2).toDouble(), z1.coerceAtLeast(z2).toDouble()
        )

    fun contains(x: Int, y: Int, z: Int, world: String?): Boolean
    {
        return y in y1..y2 && contains(x, z, world)
    }

    fun contains(x: Int, z: Int, world: String?): Boolean
    {
        return if (world != null && !world.equals(this.world, ignoreCase = true))
        {
            false
        } else x in x1..x2 && z >= z1 && z <= z2
    }

    operator fun contains(location: Location): Boolean
    {
        return contains(location.blockX, location.blockY, location.blockZ, location.world.name)
    }

    operator fun contains(block: Block): Boolean
    {
        return contains(block.location)
    }

    operator fun contains(player: Player): Boolean
    {
        return contains(player.location)
    }

    val players: Set<Player>
        get()
        {
            val players: MutableSet<Player> = HashSet()
            for (player in Bukkit.getServer().onlinePlayers)
            {
                if (contains(player)) players.add(player)
            }
            return players
        }

    override fun hashCode(): Int
    {
        return maximumPoint.hashCode() + minimumPoint.hashCode()
    }

    override fun toString(): String
    {
        val corner1 = minimumPoint
        val corner2 = maximumPoint
        return corner1.blockX.toString() + ":" + corner1.blockY + ":" + corner1.blockZ + ":" + corner2.blockX + ":" + corner2.blockY + ":" + corner2.blockZ + ":" + name + ":" + world
    }

    val friendlyName: String
        get() = "($world, $x1, $y1, $z1) - ($world, $x2, $y2, $z2)"

    fun expand(dir: CuboidDirection, amount: Int): Claim
    {
        return when (dir)
        {
            CuboidDirection.North -> Claim(
                world,
                x1 - amount,
                y1,
                z1,
                x2,
                y2,
                z2
            )
            CuboidDirection.South -> Claim(
                world,
                x1,
                y1,
                z1,
                x2 + amount,
                y2,
                z2
            )
            CuboidDirection.East -> Claim(
                world,
                x1,
                y1,
                z1 - amount,
                x2,
                y2,
                z2
            )
            CuboidDirection.West -> Claim(
                world,
                x1,
                y1,
                z1,
                x2,
                y2,
                z2 + amount
            )
            CuboidDirection.Down -> Claim(
                world,
                x1,
                y1 - amount,
                z1,
                x2,
                y2,
                z2
            )
            CuboidDirection.Up -> Claim(
                world,
                x1,
                y1,
                z1,
                x2,
                y2 + amount,
                z2
            )
            else -> throw IllegalArgumentException("Invalid direction $dir")
        }
    }

    fun outset(dir: CuboidDirection, amount: Int): Claim
    {
        val claim: Claim = when (dir)
        {
            CuboidDirection.Horizontal -> expand(
                CuboidDirection.North,
                amount
            ).expand(CuboidDirection.South, amount)
                .expand(CuboidDirection.East, amount)
                .expand(CuboidDirection.West, amount)
            CuboidDirection.Vertical -> expand(
                CuboidDirection.Down,
                amount
            ).expand(CuboidDirection.Up, amount)
            CuboidDirection.Both -> outset(
                CuboidDirection.Horizontal,
                amount
            ).outset(CuboidDirection.Vertical, amount)
            else -> throw IllegalArgumentException("Invalid direction $dir")
        }
        return claim
    }

    fun isWithin(x: Int, z: Int, radius: Int, world: String?): Boolean
    {
        return outset(CuboidDirection.Both, radius).contains(x, z, world)
    }

    fun setLocations(loc1: Location, loc2: Location)
    {
        x1 = loc1.blockX.coerceAtMost(loc2.blockX)
        x2 = loc1.blockX.coerceAtLeast(loc2.blockX)
        y1 = loc1.blockY.coerceAtMost(loc2.blockY)
        y2 = loc1.blockY.coerceAtLeast(loc2.blockY)
        z1 = loc1.blockZ.coerceAtMost(loc2.blockZ)
        z2 = loc1.blockZ.coerceAtLeast(loc2.blockZ)
    }

    val cornerLocations: Array<Location>
        get()
        {
            val world: World = Bukkit.getServer().getWorld(world)
            return arrayOf(
                Location(world, x1.toDouble(), y1.toDouble(), z1.toDouble()),
                Location(world, x2.toDouble(), y1.toDouble(), z2.toDouble()),
                Location(world, x1.toDouble(), y1.toDouble(), z2.toDouble()),
                Location(world, x2.toDouble(), y1.toDouble(), z1.toDouble())
            )
        }

    override fun iterator(): MutableIterator<Coordinate>
    {
        return BorderIterator(x1, z1, x2, z2)
    }

    enum class BorderDirection
    {
        POS_X, POS_Z, NEG_X, NEG_Z
    }

    inner class BorderIterator(x1: Int, z1: Int, x2: Int, z2: Int) : MutableIterator<Coordinate>
    {
        private var x: Int
        private var z: Int
        private var next = true
        private var dir = BorderDirection.POS_Z
        var maxX = maximumPoint.blockX
        var maxZ = maximumPoint.blockZ
        var minX = minimumPoint.blockX
        var minZ = minimumPoint.blockZ

        init
        {
            x = x1.coerceAtMost(x2)
            z = z1.coerceAtMost(z2)
        }

        override fun hasNext(): Boolean
        {
            return next
        }

        override fun next(): Coordinate
        {
            if (dir == BorderDirection.POS_Z)
            {
                if (++z == maxZ)
                {
                    dir = BorderDirection.POS_X
                }
            } else if (dir == BorderDirection.POS_X)
            {
                if (++x == maxX)
                {
                    dir = BorderDirection.NEG_Z
                }
            } else if (dir == BorderDirection.NEG_Z)
            {
                if (--z == minZ)
                {
                    dir = BorderDirection.NEG_X
                }
            } else if (dir == BorderDirection.NEG_X)
            {
                if (--x == minX)
                {
                    next = false
                }
            }
            return Coordinate(x, z)
        }

        override fun remove()
        {
        }
    }

    enum class CuboidDirection
    {
        North, East, South, West, Up, Down, Horizontal, Vertical, Both, Unknown
    }

    companion object
    {
        fun getPrice(claim: Claim, team: Team?, buying: Boolean): Int
        {
            val x = abs(claim.x1 - claim.x2)
            val z = abs(claim.z1 - claim.z2)
            var blocks = x * z
            var done = 0
            var mod = 0.4
            var curPrice = 0.0
            while (blocks > 0)
            {
                blocks--
                done++
                curPrice += mod
                if (done == 250)
                {
                    done = 0
                    mod += 0.4
                }
            }

            // Multiple price by 0.8 (requested by @itsjhalt)
            curPrice *= 0.8
            if (buying && team != null)
            {
                curPrice += 500 * team.claims.size
            }
            return curPrice.toInt()
        }
    }
}