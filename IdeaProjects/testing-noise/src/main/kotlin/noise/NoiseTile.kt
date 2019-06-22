package noise

class TriangleTile (val tile: Array<IntArray>, val bottom: Boolean) {
    operator fun get(index: Int): IntArray = tile[index]

    override fun toString(): String {
        val builder = StringBuilder()
        for(i in 0 until tile.size) {
            for(c in tile[tile.size - i - 1]) {
                builder.append(lpad0(c.toString()))
                builder.append(" ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }
}

val tilea1: Array<IntArray> =
        arrayOf(
            intArrayOf(
                1, 8, 9, 10, 11
            ),
            intArrayOf(
                2, 4, 15, 16, 12
            ),
            intArrayOf(
                3, 5, 3, 17, 13
            ),
            intArrayOf(
                4, 6, 7, 2, 14
            ),
            intArrayOf(
                1, 2, 3, 4, 1
            )
        )

val tilea2: Array<IntArray> =
    arrayOf(
        intArrayOf(
            11, 12, 13, 14, 1
        ),
        intArrayOf(
            18, 20, 24, 25, 8
        ),
        intArrayOf(
            19, 21, 19, 26, 9
        ),
        intArrayOf(
            20, 22, 23, 18, 10
        ),
        intArrayOf(
            11, 18, 19, 20, 11
        )
    )

val ttile1: TriangleTile = TriangleTile(tilea1, false)
val ttile2: TriangleTile = TriangleTile(tilea1, true)
val ttile3: TriangleTile = TriangleTile(tilea2, false)
val ttile4: TriangleTile = TriangleTile(tilea2, true)

val arrTtile: Array<Pair<TriangleTile, Int>> =
        arrayOf(
            Pair(ttile1, 0),
            Pair(ttile2, 1),
            Pair(ttile2, 0),
            Pair(ttile4, 2),
            Pair(ttile2, 2),
            Pair(ttile4, 0),
            Pair(ttile4, 1),
            Pair(ttile3, 0)
        )


fun lpad0(s: String): String {
    val l = 3 - s.length
    return "0".repeat(l) + s
}

class FractalTile(val octaves: Array<Array<SquareTile>>)

class FractalTileSimplex(val octaves: Array<Array<TriangleTile>>)

class SquareTile (val hash: Array<IntArray>, val colors: IntArray) {
    operator fun get(index: Int): IntArray = hash[index]

    override fun toString(): String {
        val builder = StringBuilder()
        for(i in 0 until hash.size) {
            for(c in hash[hash.size - i - 1]) {
                builder.append(lpad0(c.toString()))
                builder.append(" ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }
}

val tile: SquareTile =
        SquareTile(
            arrayOf(
                intArrayOf(
                    10, 34, 86, 15, 10
                ),
                intArrayOf(
                    50, 222, 4, 26, 50
                ),
                intArrayOf(
                    2, 193, 46, 67, 2
                ),
                intArrayOf(
                    128, 44, 25, 72, 128
                ),
                intArrayOf(
                    10, 34, 86, 15, 10
                )
            ),
            intArrayOf(1, 1, 1, 1)
        )


val tile1: SquareTile =
    SquareTile(
        arrayOf(
            intArrayOf(
                1, 1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1, 1
            )
        ),
        intArrayOf(1, 1, 1, 1)
    )

val tiles: SquareTile =
    SquareTile(
        arrayOf(
            intArrayOf(
                1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1
            ),
            intArrayOf(
                1, 1, 1, 1
            )
        ),
        intArrayOf(1, 1, 1)
    )