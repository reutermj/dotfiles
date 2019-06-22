package rewrite

import kotlin.random.Random


data class PermutationTable(val perm: IntArray) {
    operator fun get(x: Int): Int {
        return perm[x and 0xff]
    }

    operator fun get(x: Int, y: Int): Int {
        return this[x] xor this[y]
    }

    operator fun get(x: Int, y: Int, z: Int): Int {
        return this[x] xor this[y] xor this[z]
    }

    operator fun get(arr: IntArray, offsets: Int): Int {
        var index = 0
        for(i in 0 until arr.size) {
            val z = this[arr[i] + (offsets.ushr(i) and 0b1)]
            index = index xor z
        }
        return index
    }
}

fun perm(): PermutationTable {
    return perm(System.nanoTime(), 256)
}

fun perm(seed: Long): PermutationTable {
    return perm(seed, 256)
}

fun perm(len: Int): PermutationTable {
    return perm(System.nanoTime(), len)
}

fun perm(seed: Long, len: Int): PermutationTable {
    val rand = Random(seed)
    return perm(rand, len)
}

fun perm(rand: Random, len: Int): PermutationTable {
    val perm = IntArray(len) {it}
    for (c in 0 until 5) {
        for (i in 0 until len) {
            val r = rand.nextInt(0, len)
            val t = perm[i]
            perm[i] = perm[r]
            perm[r] = t
        }
    }
    return PermutationTable(perm)
}