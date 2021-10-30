package jbitset

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
}