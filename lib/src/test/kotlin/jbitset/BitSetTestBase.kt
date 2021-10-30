package jbitset

import org.junit.Assert
import org.junit.Before
import java.security.SecureRandom

abstract class BitSetTestBase {
    private val random = SecureRandom()
    lateinit var bb1: BitSet<Long>
    lateinit var bb2: BitSet<Long>

    abstract fun bitSetImpl( ) : BitSet<Long>

    @Before
    fun setup() {
        bb1 = bitSetImpl()
        bb2 = bitSetImpl()
    }

    enum class SetOp{
        UNION,
        INTERSECTION,
        MINUS
    }

    private fun setupEqual(maxSize: Long ){
        (0 until maxSize).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = bitValue
        }
    }

    private fun setupComplement(maxSize: Long ){
        (0 until maxSize).forEach {
            val bitValue = random.nextBoolean()
            bb1[it] = bitValue
            bb2[it] = !bitValue
        }
    }

    private fun setupSetOp(maxSize: Long, setOp: SetOp) : Set<Long> {
        // ensure actual are pretty less in numbers
       return (0 until maxSize).filter {
           val res = random.nextInt(1000)
           bb1[it] = false
           bb2[it] = false
           val bb1Set = res < 100
           val bb2Set = res in 51..149
           bb1[it] = bb1Set
           bb2[it] = bb2Set
           when ( setOp ){
               SetOp.UNION -> bb1Set || bb2Set
               SetOp.INTERSECTION -> bb1Set && bb2Set
               SetOp.MINUS -> bb1Set && !bb2Set
           }
        }.toSet()
    }

    fun getAndSetTest( inx1 : Long, inx2: Long, size: Long ){
        bb1[inx1] = true
        bb1[inx2] = true
        val onInx = (0 until size).filter { bb1[it] }
        Assert.assertEquals(inx1, onInx.first())
        Assert.assertEquals(inx2, onInx.last())
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
        setupEqual(maxSize)
        // now we do union
        var bbu = bb1.union(bb2)
        Assert.assertEquals(bb1, bbu)
        Assert.assertEquals(bb2, bbu)
        // now with disjoint pattern
        setupComplement(maxSize)
        bbu = bb1.union(bb2)
        // this guy must have all bits set, yes?
        (0 until maxSize).forEach {
            Assert.assertTrue(bbu[it])
        }
        // now with union
        val unionBits = setupSetOp( maxSize, SetOp.UNION)
        bbu = bb1.union(bb2)
        // this guy must have all bits set apart from skipped bits , yes?
        (0 until maxSize).forEach {
            val unionBitValue = unionBits.contains(it)
            Assert.assertEquals(unionBitValue, bbu[it] )
        }
    }

    fun intersectionTest( inx1: Long, inx2: Long, maxSize: Long ){
        // case 1 same set
        bb1[inx1] = true
        bb1[inx2] = true
        bb2[inx1] = true
        bb2[inx2] = true
        var bbi = bb1.intersection(bb2)
        Assert.assertEquals(bb1, bbi)
        Assert.assertEquals(bb2, bbi)
        // case 2 one is super set of another
        bb2[40] = true
        bbi = bb1.intersection(bb2)
        Assert.assertEquals(bb1, bbi)
        Assert.assertNotEquals(bb2, bbi)
        // case 3 completely disjoint
        setupComplement(maxSize)
        bbi = bb1.intersection(bb2)
        // this should be all zero, by definition
        (0 until maxSize).forEach {
            Assert.assertFalse( bbi[it])
        }
        // now specific case of intersection only
        val commonBits = setupSetOp(maxSize, SetOp.INTERSECTION)
        bbi = bb1.intersection(bb2)
        // this guy must have all common bits, and rest false, yes?
        (0 until maxSize).forEach {
            val shouldBeThere = commonBits.contains(it)
            Assert.assertEquals(shouldBeThere, bbi[it] )
        }
    }

    fun relationsTest(inx1: Long, inx2: Long, inx3: Long ){
        // exact same sets
        bb1[inx1] = true
        bb2[inx1] = true
        Assert.assertTrue( bb1.isSuperSetOf(bb2) )
        Assert.assertTrue( bb1.isSubSetOf(bb2) )
        Assert.assertTrue( bb2.isSuperSetOf(bb1) )
        Assert.assertTrue( bb2.isSubSetOf(bb1) )

        // one is proper superset
        bb1[inx2] = true
        Assert.assertTrue( bb1.isSuperSetOf(bb2) )
        Assert.assertFalse( bb2.isSuperSetOf(bb1) )
        Assert.assertTrue( bb2.isSubSetOf(bb1) )

        // try with empty set
        val nullSet = bitSetImpl()
        Assert.assertTrue( bb1.isSuperSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSubSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSuperSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSubSetOf(bb1) )
        Assert.assertTrue( bb2.isSuperSetOf(nullSet) )
        Assert.assertTrue( nullSet.isSubSetOf(bb2) )

        Assert.assertFalse( nullSet.isSuperSetOf(bb2) )
        Assert.assertFalse( nullSet.isSuperSetOf(bb1) )
        // now sets which are not related as subset or super set
        bb2[inx3] = true
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
        // disjoint ?
        setupComplement(maxSize)
        bbm = bb1.minus(bb2)
        Assert.assertEquals(bb1, bbm)
        // now intersection is to be subtracted
        val leftOverBits = setupSetOp(maxSize, SetOp.MINUS)
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