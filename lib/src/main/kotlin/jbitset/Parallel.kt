package jbitset

import java.util.*

class ThreadedTask : Thread() {
    @Volatile
    private var runLoop = true
    private val workQueue = LinkedList<() -> Unit>()
    fun shutdown(){
        runLoop = false
    }
    fun add(task: () -> Unit) {
        synchronized(task) {
            workQueue.add(task)
        }
    }

    override fun run() {
        while ( runLoop ) {
            val task = workQueue.poll()
            task?.invoke()
        }
    }
}

fun <T> Iterable<T>.parallel(poolSize: Int = 4, breakCondition: (T) -> Boolean, body: (T) -> Unit) {
    val iterator = this.iterator()
    val tPool = Array(poolSize) { ThreadedTask() }
    tPool.forEach { it.start() }
    var inx = -1
    while (iterator.hasNext()) {
        inx = (inx + 1) % poolSize
        val item = iterator.next()
        if (breakCondition(item)) break
        tPool[inx].add { body(item) }
    }
    tPool.forEach { it.shutdown() }
    while (tPool.find { it.isAlive } != null) {
        Thread.sleep(1)
    }
}

fun <T> Iterable<T>.parallelFind(poolSize: Int = 4, pred: (T) -> Boolean): T? {
    var value: T? = null
    this.parallel(poolSize, { value != null }) {
        if (pred(it)) {
            value = it
        }
    }
    return value
}

fun <T> Iterable<T>.parallelForEach(poolSize: Int = 4, body: (T) -> Unit) {
    this.parallel(poolSize, { false }) { body(it) }
}
