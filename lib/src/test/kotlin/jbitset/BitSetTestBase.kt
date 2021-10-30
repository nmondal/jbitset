package jbitset

import org.junit.Assert
import org.junit.Before
import java.security.SecureRandom

abstract class BitSetTestBase {
    val random = SecureRandom()
    lateinit var bb1: BitSet<Long>
    lateinit var bb2: BitSet<Long>

    abstract fun bitSetImpl( ) : BitSet<Long>

    @Before
    fun setup() {
        bb1 = bitSetImpl()
        bb2 = bitSetImpl()
    }

    fun getAndSetTest( inx1 : Long, inx2: Long, size: Long ){
        bb1[inx1] = true
        bb1[inx2] = true
        val onInx = (0 until size).filter { bb1[it] }
        Assert.assertEquals(0, onInx.first())
        Assert.assertEquals(42, onInx.last())
        bb1[inx1] = false
        bb1[inx2] = false
        Assert.assertFalse(bb1[inx1])
        Assert.assertFalse(bb1[inx2])
    }

    fun equalityTest(inx1 : Long, inx2: Long, inx3: Long ){
        bb1[inx1] = true
        bb1[inx2] = true
        bb2[inx1] = true
        bb2[inx2] = true
        Assert.assertEquals(bb1, bb2)
        bb2[inx3] = true
        Assert.assertNotEquals(bb1, bb2)
    }

    fun unionTest(maxSize: Long ){
        (0 until maxSize).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = bitValue
        }
        // now we do union
        var bbu = bb1.union(bb2)
        Assert.assertEquals(bb1, bbu)
        Assert.assertEquals(bb2, bbu)
        // now with disjoint pattern
        (0 until maxSize).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = !bitValue
        }
        bbu = bb1.union(bb2)
        // this guy must have all bits set, yes?
        (0 until maxSize).forEach {
            Assert.assertTrue(bbu[it])
        }
        // now with intersection
        val skippedBits = (0 until maxSize).filter {
            bb1[it] = true
            bb2[it] = true
            var skipped = false
            when ( random.nextInt(4) ){
                0 -> bb2[it] = false
                1 -> bb1[it] = false
                2 -> {
                    bb1[it] = false
                    bb2[it] = false
                    skipped = true
                }
            }
            skipped
        }.toSet()
        bbu = bb1.union(bb2)
        // this guy must have all bits set apart from skipped bits , yes?
        (0 until maxSize).forEach {
            val shouldNotBeBitValue = skippedBits.contains(it)
            Assert.assertNotEquals(shouldNotBeBitValue, bbu[it] )
        }
    }

    fun intersectionTest( maxSize: Long ){
        // case 1 same set
        bb1[0] = true
        bb1[42] = true
        bb2[0] = true
        bb2[42] = true
        var bbi = bb1.intersection(bb2)
        Assert.assertEquals(bb1, bbi)
        Assert.assertEquals(bb2, bbi)
        // case 2 one is super set of another
        bb2[40] = true
        bbi = bb1.intersection(bb2)
        Assert.assertEquals(bb1, bbi)
        Assert.assertNotEquals(bb2, bbi)
        // case 3 completely disjoint
        (0 until maxSize).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = !bitValue
        }
        bbi = bb1.intersection(bb2)
        // this should be all zero, by definition
        (0 until maxSize).forEach {
            Assert.assertFalse( bbi[it])
        }
        // now specific case of intersection only
        val commonBits = (0 until maxSize).filter {
            bb1[it] = true
            bb2[it] = true
            var common = false
            when ( random.nextInt(4) ){
                0 -> bb2[it] = false
                1 -> bb1[it] = false
                2 -> {
                    bb1[it] = false
                    bb2[it] = false
                }
                3 -> common = true
            }
            common
        }.toSet()
        bbi = bb1.intersection(bb2)
        // this guy must have all common bits, and rest false, yes?
        (0 until maxSize).forEach {
            val shouldBeThere = commonBits.contains(it)
            Assert.assertEquals(shouldBeThere, bbi[it] )
        }
    }

    fun relationsTest(){
        // exact same sets
        bb1[0] = true
        bb2[0] = true
        Assert.assertTrue( bb1.isSuperSetOf(bb2) )
        Assert.assertTrue( bb1.isSubSetOf(bb2) )
        Assert.assertTrue( bb2.isSuperSetOf(bb1) )
        Assert.assertTrue( bb2.isSubSetOf(bb1) )

        // one is proper superset
        bb1[42] = true
        Assert.assertTrue( bb1.isSuperSetOf(bb2) )
        Assert.assertFalse( bb2.isSuperSetOf(bb1) )
        Assert.assertTrue( bb2.isSubSetOf(bb1) )

        // try with empty set
        val nullSet = BitBuffer()
        Assert.assertTrue( bb1.isSuperSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSubSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSuperSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSubSetOf(bb1) )
        Assert.assertTrue( bb2.isSuperSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSubSetOf(bb2) )

        Assert.assertFalse( nullSet.isSuperSetOf(bb2) )
        Assert.assertFalse( nullSet.isSuperSetOf(bb1) )
        // now sets which are not related as subset or super set
        bb2[1] = true
        Assert.assertFalse( bb1.isSuperSetOf(bb2) )
        Assert.assertFalse( bb2.isSuperSetOf(bb1) )
        Assert.assertFalse( bb1.isSubSetOf(bb2) )
        Assert.assertFalse( bb2.isSubSetOf(bb1) )
    }

    fun minusTest(maxSize: Long){
        // for all of these bb2 is null set or empty set
        bb1[0] = true
        var bbm = bb1.minus(bb2)
        Assert.assertEquals( bb1, bbm )
        bbm = bb2.minus(bb1)
        Assert.assertEquals( bb2, bbm )
        bbm = bb1.minus(bb1)
        Assert.assertEquals( bb2, bbm )
        // now intersection is to be subtracted
        val leftOverBits = (0 until maxSize).filter {
            bb1[it] = true
            bb2[it] = true
            var leftOver = false
            when ( random.nextInt(4) ){
                0 -> {
                    bb2[it] = false
                    leftOver = true
                }
                1 -> bb1[it] = false
                2 -> {
                    bb1[it] = false
                    bb2[it] = false
                }

            }
            leftOver
        }.toSet()
        bbm = bb1.minus(bb2)
        // this guy must have all common bits, and rest false, yes?
        (0 until maxSize).forEach {
            val shouldBeThere = leftOverBits.contains(it)
            Assert.assertEquals(shouldBeThere, bbm[it] )
        }
    }

    fun mutableOperationsTest(maxSize: Long ){
        bb1[0] = true
        bb1.mutableUnion(bb2)
        Assert.assertTrue(bb1[0])
        ( 1 until  maxSize).forEach {  Assert.assertFalse(bb1[it]) }
        bb1.mutableMinus(bb2)
        Assert.assertTrue(bb1[0])
        ( 1 until  maxSize).forEach {  Assert.assertFalse(bb1[it]) }
        bb1.mutableIntersection(bb2)
        ( 0 until  maxSize).forEach {  Assert.assertFalse(bb1[it]) }
    }
}