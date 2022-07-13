package dev.foraged.foxtrot.team.claim

data class Coordinate(var x: Int = 0, var z: Int = 0)
{
    override fun toString(): String { return "$x, $z" }
}