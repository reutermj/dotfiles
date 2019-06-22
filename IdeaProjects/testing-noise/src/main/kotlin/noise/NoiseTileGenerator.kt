package noise

import kotlin.random.Random

fun pow(n: Int, e: Int): Int {
    var i = 1
    for(c in 0 until e) {
        i *= n
    }
    return i
}

fun genSquareCornerTiles(numColors: Int, size: Int): Array<SquareTile> {
    val random = Random(System.nanoTime())

    val corners = IntArray(numColors) {
        random.nextInt(256)
    }

    val sides = Array(numColors) { c0 ->
        Array(numColors) { c1 ->
            IntArray(size + 1) {
                when (it) {
                    0 -> corners[c0]
                    size -> corners[c1]
                    else -> random.nextInt(256)
                }
            }
        }
    }

    val tiles = Array(256) { SquareTile(arrayOf(corners), corners) }

    for(c0 in 0 until numColors) {
        for(c1 in 0 until numColors) {
            for(c2 in 0 until numColors) {
                for(c3 in 0 until numColors) {
                    val tile =
                        Array(size + 1) {
                            y ->
                            IntArray(size + 1) {
                                x ->
                                when(y) {
                                    0 -> sides[c0][c1][x]
                                    size -> sides[c2][c3][x]
                                    else ->
                                        when(x) {
                                            0 -> sides[c0][c2][y]
                                            size -> sides[c1][c3][y]
                                            else -> random.nextInt(256)
                                        }
                                }
                            }
                        }

                    tiles[c0 or (c1 shl 2) or (c2 shl 4) or (c3 shl 6)] = SquareTile(tile, intArrayOf(c0, c1, c2, c3))
                }
            }
        }
    }
    
    return tiles
}

fun genTriangleTiles(numColors: Int, size: Int): Array<TriangleTile> {
    val random = Random(System.nanoTime())

    val corners = IntArray(numColors) {
        random.nextInt(256)
    }

    val vert = Array(numColors) { c0 ->
        Array(numColors) { c1 ->
            IntArray(size + 1) {
                when(it) {
                    0 -> corners[c0]
                    size -> corners[c1]
                    else -> random.nextInt(256)
                }
            }
        }
    }

    val horz = Array(numColors) { c0 ->
        Array(numColors) { c1 ->
            IntArray(size + 1) {
                when(it) {
                    0 -> corners[c0]
                    size -> corners[c1]
                    else -> random.nextInt(256)
                }
            }
        }
    }

    val diag = Array(numColors) { c0 ->
        Array(numColors) { c1 ->
            IntArray(size + 1) {
                when(it) {
                    0 -> corners[c0]
                    size -> corners[c1]
                    else -> random.nextInt(256)
                }
            }
        }
    }

    val tiles = Array(64) {TriangleTile(arrayOf(corners), false)}

    for(c0 in 0 until numColors) {
        for(c1 in 0 until numColors) {
            for(c2 in 0 until numColors) {
                    val tile =
                        Array(size + 1) {
                                y ->
                            IntArray(size + 1) {
                                    x ->
                                when(y) {
                                    0 -> vert[c0][c1][x]
                                    size -> vert[c1][c2][x]
                                    x -> diag[c0][c2][x]
                                    else ->
                                        when(x) {
                                            0 -> horz[c0][c1][y]
                                            size -> horz[c1][c2][y]
                                            else -> random.nextInt(256)
                                        }
                                }
                            }
                        }
                    tiles[c0 or (c1 shl 2) or (c2 shl 4)] = TriangleTile(tile, false)
            }
        }
    }

    return tiles
}