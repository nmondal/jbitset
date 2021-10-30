package jbitset

import kotlin.test.Test

class BitBufferTest : BitSetTestBase() {

    override fun bitSetImpl() = BitBuffer()

    @Test
    fun testGetAndSet() {
        getAndSetTest( 0, 42, bb1.size)
    }

    @Test
    fun testEquality() {
        equalityTest(0,42,1)
    }

    @Test
    fun testUnion() {
       unionTest(bb1.size)
    }

    @Test
    fun testIntersection(){
        intersectionTest(0, 42, bb1.size)
    }

    @Test
    fun testSetRelations(){
        relationsTest(0, 42, 1 )
    }

    @Test
    fun testMinus(){
        minusTest(bb1.size)
    }

    @Test
    fun testMutableOperations(){
        mutableOperationsTest(bb1.size)
    }
}
