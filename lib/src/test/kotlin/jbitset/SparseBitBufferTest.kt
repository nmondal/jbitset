package jbitset

import org.junit.Assert
import org.junit.Test

class SparseBitBufferTest {
    @Test
    fun testBasicSparseBitBuffer(){
        val sb1 = SparseBitSet()
        val sb2 = SparseBitSet()
        Assert.assertEquals(sb1, sb2 )
    }
}