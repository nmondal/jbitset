package jbitset

const val noOfBitsInLong = 64

class BitBuffer(arraySize: Int = 64, from: Array<Long>? = null) : BitSet<Long> {

    internal var array = from?.copyOf() ?: Array<Long>(arraySize) { 0 }

    override val size: Long = noOfBitsInLong * array.size.toLong()

    override operator fun get(inx: Long): Boolean {
        val i = (inx / noOfBitsInLong).toInt()
        val offset = inx % noOfBitsInLong
        val mask = 1L shl (noOfBitsInLong - offset).toInt()
        return (0L != array[i] and mask)
    }

    override operator fun set(inx: Long, value: Boolean) {
        val i = (inx / noOfBitsInLong).toInt()
        val offset = inx % noOfBitsInLong
        val mask = 1L shl (noOfBitsInLong - offset).toInt()
        if ( value ) {
            array[i] = array[i] or mask
        } else {
            array[i] = array[i] and mask.inv()
        }
    }

    override fun isSuperSetOf(other: BitSet<Long>) = when {
        other !is BitBuffer -> false
        this.size != other.size -> false
        else -> null == (array.indices).parallelFind { (other.array[it] and array[it]) != other.array[it] }
    }

    private fun checkedRun( that : BitSet<*> , holderInit : () -> Array<Long>,
                            runOperation : ( BitBuffer, Array<Long>) -> Unit ) : Array<Long> {
        if ( that !is BitBuffer || this.size != that.size ) throw IllegalArgumentException()
        val holder = holderInit()
        runOperation( that, holder)
        return holder
    }

    private fun union(that: BitSet<Long>, holderInit : () -> Array<Long>) : Array<Long> {
        return checkedRun( that , holderInit ){ other, holder ->
            array.indices.parallelForEach {
                holder[it] = array[it] or other.array[it]
            }
        }
    }

    override fun union(other: BitSet<Long>) = BitBuffer(from = union(other) { Array(array.size) { 0 } })

    override fun mutableUnion(other: BitSet<Long>) { union(other){ array } }

    private fun intersection(that: BitSet<Long>, holderInit: () -> Array<Long>) : Array<Long> {
        return checkedRun(that, holderInit ) { other, holder ->
            array.indices.parallelForEach { holder[it] = array[it] and other.array[it] }
        }
    }

    override fun intersection(other: BitSet<Long>) = BitBuffer(from = intersection(other){ Array<Long>(array.size){ 0 }})

    override fun mutableIntersection(other: BitSet<Long>) { intersection(other) { array } }

    private fun minus(that: BitSet<Long>, holderInit: () -> Array<Long>) : Array<Long> {
        return checkedRun(that, holderInit ){ other, holder ->
            array.indices.parallelForEach { holder[it] = array[it] and other.array[it].inv() }
        }
    }

    override fun minus(other: BitSet<Long>) = BitBuffer(from = minus( other ){ Array<Long>(array.size) { 0 } } )

    override fun mutableMinus(other: BitSet<Long>) { minus(other) { this.array } }

    override fun equals(other: Any?) = when {
        other === this -> true
        other !is BitBuffer -> false
        array.size != other.array.size -> false
        else -> null == (array.indices).parallelFind { other.array[it] != array[it] }
    }

    override fun hashCode() = array.contentHashCode()

    override fun clearAll() {
        array.indices.forEach { array[it] = 0 }
    }

    val allZero: Boolean
        get() = null == array.indices.parallelFind { array[it] != 0L  }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toString(): String {
        return array.indices.joinToString("") { array[it].toUInt().toString(radix = 2) }
    }
}