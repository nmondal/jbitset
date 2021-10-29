package jbitset

import org.junit.Assert
import java.security.SecureRandom
import kotlin.test.BeforeTest
import kotlin.test.Test

class BitBufferTest {

    private val random = SecureRandom()
    lateinit var bb1: BitBuffer
    lateinit var bb2: BitBuffer
    lateinit var bb3: BitBuffer

    @BeforeTest
    fun setup() {
        bb1 = BitBuffer()
        bb2 = BitBuffer()
        bb3 = BitBuffer()
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
}
