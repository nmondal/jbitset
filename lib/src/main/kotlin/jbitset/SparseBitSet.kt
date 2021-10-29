package jbitset

import kotlin.concurrent.thread

class SparseBitSet(private var pages : MutableMap<Long,BitBuffer> = mutableMapOf()) : BitSet<Long> {

    private val internalBitBufferSize = 64

    private val pageSize = 64 * internalBitBufferSize

    private var maxPageNum = 0L

    override fun get(inx: Long) = pages[inx / pageSize]?.let { it[inx % pageSize] }?:false

    private fun gc(pageInx: Long){
        thread {
            pages[pageInx]?.let{ bitBuffer ->
                val allZeros =  null == bitBuffer.array.indices.parallelFind { bitBuffer.array[it] != 0L  }
                if ( allZeros ){
                    synchronized( this ) {
                        pages.remove(pageInx)
                        if ( pageInx == maxPageNum ){ maxPageNum = pages.keys.maxOrNull() ?:0 }
                    }
                }
            }
        }
    }

    override fun set(inx: Long, value: Boolean) {
        val pageInx = inx/pageSize
        var page = pages[pageInx]
        if ( page == null ){
            if ( value ){
                page = BitBuffer()
                pages[pageInx] = page
            } else {
                return
            }
        }
        page[inx % pageSize ] = value
        if ( pageInx > maxPageNum ){
            maxPageNum = pageInx
        }
        // now, if this is false, and the whole page is zero, what to do?
        if ( !value ) gc(pageInx)
    }

    override fun isSuperSetOf(other: BitSet<Long>): Boolean {
        return when {
            other !is SparseBitSet -> false
            other === this -> true
            pages.size < other.pages.size -> false
            !pages.keys.containsAll(other.pages.keys) -> false
            else -> null == other.pages.keys.parallelFind { !pages[it]!!.isSuperSetOf(other.pages[it]!!) }
        }
    }

    private fun checkedRun( that : BitSet<*> , holderInit : () ->MutableMap<Long,BitBuffer>,
                            runOperation : ( SparseBitSet, MutableMap<Long,BitBuffer>) -> Unit ) : MutableMap<Long,BitBuffer> {
        if ( that !is SparseBitSet  ) throw IllegalArgumentException()
        val holder = holderInit()
        runOperation( that, holder)
        return holder
    }

    private fun minus( that: BitSet<Long>, holderInit :  () -> MutableMap<Long,BitBuffer> ) : MutableMap<Long,BitBuffer>  {
        return checkedRun( that , holderInit ){ other, holder ->
            other.pages.entries.parallelForEach { entry  ->
                holder[entry.key]?.let { bitBuffer ->
                    holder[entry.key] = bitBuffer.minus( entry.value )
                }
            }
        }
    }

    override fun minus(other: BitSet<Long>) = SparseBitSet( minus( other) {  pages.toMutableMap() })

    override fun mutableMinus(other: BitSet<Long>) { minus(other){ pages } }

    private fun union( that: BitSet<Long>, holderInit : () -> MutableMap<Long,BitBuffer> ) : MutableMap<Long,BitBuffer> {
        return checkedRun( that , holderInit ){ other, holder ->
            other.pages.entries.parallelForEach { entry  ->
                holder[entry.key]?.let {
                    holder[entry.key] = holder[entry.key]?.union( entry.value )
                        ?: BitBuffer(from = entry.value.array.copyOf())
                }
            }
        }
    }

    override fun union(other: BitSet<Long>) = SparseBitSet( union( other) { pages.toMutableMap() })

    override fun mutableUnion(other: BitSet<Long>) { union(other){ pages } }

    private fun intersection( that: BitSet<Long>, holderInit : () -> MutableMap<Long,BitBuffer> ) : MutableMap<Long,BitBuffer> {
        return checkedRun( that , holderInit ){ other, holder ->
            other.pages.entries.parallelForEach { entry  ->
                holder[entry.key]?.let { bitBuffer ->
                    holder[entry.key] = bitBuffer.intersection( entry.value )
                }
            }
        }
    }

    override fun intersection(other: BitSet<Long>) = SparseBitSet( intersection(other) { pages.toMutableMap()})

    override fun mutableIntersection(other: BitSet<Long>) { intersection(other) { pages } }

    override val size: Long = maxPageNum  *  pageSize * 64L

    override fun equals(other: Any?) = when {
        other !is SparseBitSet -> false
        other === this -> true
        pages.size != other.pages.size -> false
        !pages.keys.containsAll(other.pages.keys) -> false
        else -> null == other.pages.entries.parallelFind { it.value != pages[it.key] }
    }

    override fun hashCode() = pages.hashCode()
}