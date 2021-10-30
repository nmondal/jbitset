package jbitset

interface BitSet<T : Number> {
    operator fun get(inx: T): Boolean
    operator fun set(inx: T, value: Boolean)
    fun isSuperSetOf(other: BitSet<T>): Boolean
    fun isSubSetOf(other: BitSet<T>) = other.isSuperSetOf(this)
    fun minus(other: BitSet<T>): BitSet<T>
    fun union(other: BitSet<T>): BitSet<T>
    fun intersection(other: BitSet<T>): BitSet<T>
    fun mutableMinus(other: BitSet<T>)
    fun mutableUnion(other: BitSet<T>)
    fun mutableIntersection(other: BitSet<T>)
    fun clearAll()
    val size: Long
    fun indices() : Iterator<T>
}

