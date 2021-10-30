package jbitset

import org.junit.Assert
import org.junit.Test
import kotlin.math.absoluteValue

class SparseBitSetTest : BitSetTestBase(){

    override fun bitSetImpl() = SparseBitSet()

    private val nullSet = bitSetImpl()

    private fun sparseIndices(maxRange: Int ) : MutableSet<Long> {
        return (1..maxRange).map{
            val inx =  random.nextInt( Int.MAX_VALUE ).toLong().absoluteValue
            bb1[inx] = true
            bb2[inx] = true
            inx
        }.toMutableSet()
    }

    private fun randomIndicesTest( bitSet: BitSet<Long>, onIndices : Set<Long> , trials: Int  ){
        (1..trials).forEach { _ ->
            val inx =  random.nextInt( Int.MAX_VALUE ).toLong().absoluteValue
            val shouldBeValue =  onIndices.contains(inx)
            Assert.assertEquals( shouldBeValue, bitSet[inx] )
        }
    }

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
        val onIndices = sparseIndices(100)
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
        // test randomly if all other indices are zero
        randomIndicesTest( bbu, onIndices, 10000 )
    }

    @Test
    fun testIntersection(){
        val onIndices = sparseIndices(200)
        Assert.assertEquals(bb1,bb2) // they should be same
        // now do an intersection producing nothing
        var bbi = bb1.intersection(nullSet)
        Assert.assertEquals(nullSet,bbi)

        bbi = bb1.intersection(bb2) // intersection should produce the rest
        Assert.assertEquals(bb1,bbi)
        Assert.assertEquals(bb2,bbi)

        // now different values
        (1..100).map {
            val inx = random.nextInt(Int.MAX_VALUE).toLong().absoluteValue
            Pair(it, inx)
        }.filter { !onIndices.contains(it.second ) }.forEach {
            if ( it.first % 2 == 0 ){
                bb1[it.second] = true
            } else {
                bb2[it.second] = true
            }
        }
        bbi = bb1.intersection(bb2)
        onIndices.forEach { Assert.assertTrue(bbi[it]) }
        // test randomly if all other indices are zero
        randomIndicesTest( bbi, onIndices, 11000 )
    }

    @Test
    fun testRelations(){
        relationsTest( 0, 10000000, 10000000000 )
    }

    @Test
    fun testMinus(){
        val onIndices = sparseIndices(800)
        Assert.assertEquals(bb1,bb2) // they should be same
        var bbm = bb1.minus(bb2)
        Assert.assertEquals(nullSet,bbm)
        onIndices.clear()
        (1..500).forEach{
            val inx =  random.nextInt( Int.MAX_VALUE ).toLong().absoluteValue
            if ( it % 2 == 0 ){
                bb1[inx] = true
                onIndices.add(inx)
            } else {
                bb2[inx] = true
            }
        }
        bbm = bb1.minus(bb2)
        onIndices.forEach { Assert.assertTrue( bbm[it]) }
        // test randomly if all other indices are zero
        randomIndicesTest( bbm, onIndices, 10000 )
    }

    @Test
    fun testMutableOperations(){

    }
}