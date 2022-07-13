package dev.foraged.foxtrot.team.claim

class CoordinateSet(x: Int, z: Int)
{
    private val x: Int
    private val z: Int

    init {
        this.x = x shr BITS
        this.z = z shr BITS
    }

    override fun equals(obj: Any?): Boolean
    {
        if (obj == null || javaClass != obj.javaClass)
        {
            return false
        }
        val other = obj as CoordinateSet
        return other.x == x && other.z == z
    }

    override fun hashCode(): Int
    {
        var hash = 5
        hash = 37 * hash + x
        hash = 37 * hash + z
        return hash
    }

    companion object
    {
        const val BITS = 6
    }
}