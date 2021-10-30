package jbitset

import org.junit.Test

class SparseBitBufferTest : BitSetTestBase(){

    override fun bitSetImpl() = SparseBitSet()

    @Test
    fun testGetAndSet() {
        val inx2 = 1000000L
        getAndSetTest( 0, inx2, inx2+1)
    }
}