package jbitset

import org.junit.Assert
import org.junit.Test
import kotlin.math.absoluteValue

class SparseBitSetTest : BitSetTestBase(){

    override fun bitSetImpl() = SparseBitSet()

    @Test
    fun testGetAndSet() {
        val inx2 = 1000000L
        getAndSetTest( 0, inx2, inx2+1)
    }

    @Test
    fun testEquality() {
        equalityTest(0,1000000,100000)
    }

    @Test
    fun testUnion() {
        // these are to be done sparsely else BOOOM! on memory and CPU
        val onIndices = (1..100).map{
            val inx =  random.nextInt( Int.MAX_VALUE ).toLong().absoluteValue
            bb1[inx] = true
            bb2[inx] = true
            inx
        }.toMutableSet()

        Assert.assertEquals(bb1,bb2) // they should be same
        var bbu = bb1.union(bb2) // union should produce the rest
        Assert.assertEquals(bb1,bbu)
        Assert.assertEquals(bb2,bbu)
        // now different values
        (1..50).forEach{
            val inx =  random.nextInt( Int.MAX_VALUE ).toLong().absoluteValue
            if ( it % 2 == 0 ){
               bb1[inx] = true
            } else {
                bb2[inx] = true
            }
            onIndices.add(inx)
        }
        bbu = bb1.union(bb2)
        onIndices.forEach { Assert.assertTrue(bbu[it]) }
    }
}