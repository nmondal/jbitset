package jbitset

import org.junit.Assert
import java.security.SecureRandom
import kotlin.test.BeforeTest
import kotlin.test.Test

class BitBufferTest {

    private val random = SecureRandom()
    private lateinit var bb1: BitSet<Long>
    private lateinit var bb2: BitSet<Long>

    @BeforeTest
    fun setup() {
        bb1 = BitBuffer()
        bb2 = BitBuffer()
    }

    @Test
    fun testGetAndSet() {
        bb1[0] = true
        bb1[42] = true
        val onInx = (0 until bb1.size).filter { bb1[it] }
        Assert.assertEquals(0, onInx.first())
        Assert.assertEquals(42, onInx.last())
        bb1[0] = false
        bb1[42] = false
        Assert.assertFalse(bb1[0])
        Assert.assertFalse(bb1[42])
    }

    @Test
    fun testEquality() {
        bb1[0] = true
        bb1[42] = true
        bb2[0] = true
        bb2[42] = true
        Assert.assertEquals(bb1, bb2)
        bb2[1] = true
        Assert.assertNotEquals(bb1, bb2)
    }

    @Test
    fun testUnion() {
        (0 until bb1.size).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = bitValue
        }
        // now we do union
        var bbu = bb1.union(bb2)
        Assert.assertEquals(bb1, bbu)
        Assert.assertEquals(bb2, bbu)
        // now with disjoint pattern
        (0 until bb1.size).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = !bitValue
        }
        bbu = bb1.union(bb2)
        // this guy must have all bits set, yes?
        (0 until bbu.size).forEach {
            Assert.assertTrue(bbu[it])
        }
        // now with intersection
        val skippedBits = (0 until bb1.size).filter {
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
        (0 until bbu.size).forEach {
            val shouldNotBeBitValue = skippedBits.contains(it)
            Assert.assertNotEquals(shouldNotBeBitValue, bbu[it] )
        }
    }

    @Test
    fun testIntersection(){
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
        (0 until bb1.size).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = !bitValue
        }
        bbi = bb1.intersection(bb2)
        // this should be all zero, by definition
        (0 until bbi.size).forEach {
            Assert.assertFalse( bbi[it])
        }
        // now specific case of intersection only
        val commonBits = (0 until bb1.size).filter {
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
        (0 until bbi.size).forEach {
            val shouldBeThere = commonBits.contains(it)
            Assert.assertEquals(shouldBeThere, bbi[it] )
        }
    }

    @Test
    fun testSetRelations(){
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
}
