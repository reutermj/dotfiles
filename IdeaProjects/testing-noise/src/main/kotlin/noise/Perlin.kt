package noise

import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.sqrt

fun ffloor(f: Float): Int {
    if (f >= 0) return f.toInt()
    else return f.toInt() - 1
}

private fun dot(g: Vector1, x: Float): Float {
    return g.x * x
}

private fun dot(g: Vector2, x2: Float, y2: Float): Float {
    return (g.x * x2) + (g.y * y2)
}

private fun dot(g: Vector3, x2: Float, y2: Float, z2: Float): Float {
    return (g.x * x2) + (g.y * y2) + (g.z * z2)
}

private fun lerp(a: Float, b: Float, t: Float): Float {
    return ((1 - t) * a) + (t * b)
}

private fun interp(t: Float): Float {
    return t * t * t * (t * (t * 6 - 15) + 10)
}

val r2: Float = sqrt(2f)

val maxes: Array<Float> = Array(5) {-2f}
val mins: Array<Float> = Array(5) {2f}

fun fractal(grads: Gradients<Vector1>, perm: PermutationTable, x: Float, octaves: Int, lacunarity: Float, f: (Gradients<Vector1>, PermutationTable, Float) -> Float): Float {
    var noise = 0f

    val gain = 1 / lacunarity

    var amplitude = 1f
    var frequency = 1f

    var max = 0f

    for (i in 0 until octaves) {
        max += amplitude
        val n = f(grads, perm, frequency * x)
        noise += amplitude * n
        frequency *= lacunarity
        amplitude *= gain
    }

    return noise / max
}

fun fractal(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float, octaves: Int, lacunarity: Float, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): Float {
    var noise = 0f

    val gain = 1 / lacunarity

    var amplitude = 1f
    var frequency = 1f

    var max = 0f

    for (i in 0 until octaves) {
        max += amplitude
        val n = f(grads, perm, frequency * x, frequency * y)
        noise += amplitude * n
        frequency *= lacunarity
        amplitude *= gain
    }

    return noise / max
}

fun fractalv2(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float, octaves: Int): Float {
    var noise = 0f

    val lacunarity = 2.5f
    val gain = 1 / lacunarity

    var amplitude = 1f
    var frequency = 1f

    var max = 0f

    for (i in 0 until octaves) {
        max += amplitude
        val n = perlinRadial2v2(grads, perm, frequency * x, frequency * y)
        //if(n > maxes[i]) maxes[i] = n
        //if(n < mins[i]) mins[i] = n
        noise += amplitude * n
        frequency *= lacunarity
        amplitude *= gain
    }

    return noise / max
}

fun simplexV2(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<Pair<TriangleTile, Int>>, x: Float, y: Float): Float {
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val xs = x + s
    val ys = y + s
    val i = ffloor(xs)
    val j = ffloor(ys)

    //println("i: $i, j: $j")

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    //find bottom left coords of the tile
    val tx = i shr -4
    val ty = j shr -4

    //if(xs >= i - 0.03f && xs <= i + 0.03f && ys >= j - 0.03f && ys <= j + 0.03f) {
        //println("xs: $xs. ys: $ys")
        //
    //}

    //find the color of the bottom left corner of the tile
    val c0 = (perm.perm[tx and 0xff] xor perm.perm[ty and 0xff])


    //find the bottom left corner of the int lattice as an offset from the bottom left corner of the tile
    val lx = i and 0x3
    val ly = j and 0x3
    //println(" i: $i, j: $j. tx: $tx. ty: $ty. lx: $lx. ly: $ly")

    val isObjectSpaceBottomTile = lx >= ly

    //get other corner colors and the tile index
    val tileIndex =
        if(isObjectSpaceBottomTile) {
            val c1 = (perm.perm[(tx + 1) and 0xff] xor perm.perm[ty and 0xff])
            val c2 = (perm.perm[(tx + 1) and 0xff] xor perm.perm[(ty + 1) and 0xff])
            //println("bottom tri")
            //println("c0: $c0. c1: $c1. c2: $c2")
            (c0 and 0x1) or (c1 and 0x1 shl 1) or (c2 and 0x2 shl 1)
        } else {
            val c1 = (perm.perm[tx and 0xff] xor perm.perm[(ty + 1) and 0xff])
            val c2 = (perm.perm[(tx + 1) and 0xff] xor perm.perm[(ty + 1) and 0xff])
            //println("top tri")
            //println("c0: $c0. c1: $c1. c2: $c2")
            (c0 and 0x1) or (c1 and 0x1 shl 1) or (c2 and 0x2 shl 1)
        }

    //get the tile the number of rotations and is the tile a bottom or top triangle
    val pair = tiles[0]
    val tile = pair.first
    val rot = pair.second
    val isTileSpaceBottomTile = tile.bottom
    //println("rot: $rot")

    //println(rot)

    val isObjectSpaceBottomTriangle = x0 >= y0
    val doesFlip = (isObjectSpaceBottomTile xor isTileSpaceBottomTile)
    val isTileSpaceBottomTriangle = doesFlip xor isObjectSpaceBottomTriangle //im brilliant

    //find the lattice coords of the first point in the tile
    val (tlx, tly) =
            if(doesFlip) {
                //println("flip")
                Pair(ly, lx)
            } else {
                //println("noflip")
                Pair(lx, ly)
            }

    //rotate the lattice appropriately
    val (rtlx0, rtly0) =
            if(isTileSpaceBottomTile) {
                when (rot) {
                    0 -> Pair(tlx, tly)
                    1 -> Pair(4 - tly, tlx - tly)
                    else -> Pair(4 - tlx + tly, 4 - tlx)
                }
            } else {
                when (rot) {
                    0 -> Pair(tlx, tly)
                    1 -> Pair(tly - tlx, 4 - tlx)
                    else -> Pair(4 - tly, 4 + tlx - tly)
                }
            }

    val (rtlx1, rtly1) =
            if(isTileSpaceBottomTile) {
                when (rot) {
                    0 -> Pair(rtlx0 + 1, rtly0)
                    1 -> Pair(rtlx0, rtly0 + 1)
                    else -> Pair(rtlx0 - 1, rtly0 - 1)
                }
            } else {
                when (rot) {
                    0 -> Pair(rtlx0, rtly0 + 1)
                    1 -> Pair(rtlx0 + 1, rtly0)
                    else -> Pair(rtlx0 - 1, rtly0 - 1)
                }
            }

    val (rtlx2, rtly2) =
            if(isTileSpaceBottomTile) {
                when (rot) {
                    0 -> Pair(rtlx0 + 1, rtly0 + 1)
                    1 -> Pair(rtlx0 - 1, rtly0)
                    else -> Pair(rtlx0 - 1, rtly0 - 1)
                }
            } else {
                when (rot) {
                    0 -> Pair(rtlx0 + 1, rtly0 + 1)
                    1 -> Pair(rtlx0, rtly0 - 1)
                    else -> Pair(rtlx0 - 1, rtly0)
                }
            }
    //println("i: $i, j: $j. tx: $tx. ty: $ty. lx: $lx. ly: $ly")
    //println("flip?: $doesFlip. rot: $rot")
    //println("rtlx0: $rtlx0, rtly0: $rtly0. rtlx1: $rtlx1, rtly1: $rtly1. rtlx2: $rtlx2, rtly2: $rtly2")
    val g0 = grads.grads[tile[rtly0][rtlx0].rem(grads.grads.size)]
    val g1 = grads.grads[tile[rtly1][rtlx1].rem(grads.grads.size)]
    val g2 = grads.grads[tile[rtly2][rtlx2].rem(grads.grads.size)]

    var i1 = 0
    var j1 = 0
    if(x0 > y0) {
        i1 = 1
        j1 = 0
    } else {
        i1 = 0
        j1 = 1
    }

    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x0 - 1f + 2f * G2
    val y2 = y0 - 1f + 2f * G2

    //val g0 = g(grads, perm, i, j)
    //val g1 = g(grads, perm, i + i1, j + j1)
    //val g2 = g(grads, perm, i + 1, j + 1)

    val t0 = 0.5f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 0.5f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 0.5f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    return 70f * (n0 + n1 + n2)
}

fun simplexTiles(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<TriangleTile>, x: Float, y: Float): Float {
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val xs = x + s
    val ys = y + s
    val i = ffloor(xs)
    val j = ffloor(ys)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    var i1 = 0
    var j1 = 1
    if(x0 >= y0) { // bottom triangle
        i1 = 1
        j1 = 0
    }

    val li = i and 0x3
    val lj = j and 0x3

    var i2 = 0
    var j2 = 1
    if(li > lj) {
        i2 = 1
        j2 = 0
    } else if(li == lj) {
        i2 = i1
        j2 = j1
    }

    val ti = i shr 2
    val tj = j shr 2

    val c0 = (perm.perm[ti and 0xff] xor perm.perm[tj and 0xff]) and 0b11
    val c1 = (perm.perm[(ti + i2) and 0xff] xor perm.perm[(tj + j2) and 0xff]) and 0b11
    val c2 = (perm.perm[(ti + 1) and 0xff] xor perm.perm[(tj + 1) and 0xff]) and 0b11

    val tile = tiles[c0 or (c1 shl 2) or (c2 shl 4)]

    val g0 = grads.grads[tile[lj][li].rem(grads.grads.size)]
    val g1 = grads.grads[tile[lj + j1][li + i1].rem(grads.grads.size)]
    val g2 = grads.grads[tile[lj + 1][li + 1].rem(grads.grads.size)]


    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x0 - 1f + 2f * G2
    val y2 = y0 - 1f + 2f * G2

    val t0 = 0.5f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 0.5f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 0.5f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    return 70f * (n0 + n1 + n2)
}

private val G2 = (3f - sqrt(3f))/6f

fun simplexSimp(x0: Float, y0: Float, i1: Int, j1: Int, g0: Vector2, g1: Vector2, g2: Vector2): Float {
    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x0 - 1f + 2f * G2
    val y2 = y0 - 1f + 2f * G2

    val t0 = 0.5f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 0.5f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 0.5f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    return 70f * (n0 + n1 + n2)
}

fun simplexFractalTiles(grads: Gradients<Vector2>, perm: PermutationTable, fractals: FractalTileSimplex, x: Float, y: Float): Float {
    var noise = 0f

    val lacunarity = 2f
    val gain = 0.5f

    var amplitude = 0.5f
    var frequency = 1f

    var xt = x
    var yt = y

    val F2 = 0.5f * (sqrt(3f)-1f)
    var s = (xt + yt) * F2
    var xs = xt + s
    var ys = yt + s

    var i = ffloor(xs)
    var j = ffloor(ys)

    var t = (i + j) * G2

    var X0 = i - t
    var Y0 = j - t
    var x0 = xt - X0
    var y0 = yt - Y0

    var i1 = 0
    var j1 = 1
    if(x0 >= y0) { // bottom triangle
        i1 = 1
        j1 = 0
    }

    var li = i and 0x3
    var lj = j and 0x3

    var i2 = 0 // just used for corner colors
    var j2 = 1
    if(li > lj) {
        i2 = 1
        j2 = 0
    } else if(li == lj) {
        i2 = i1
        j2 = j1
    }

    val ti = i shr 2
    val tj = j shr 2

    val c0 = (perm.perm[ti and 0xff] xor perm.perm[tj and 0xff]) and 0b11
    val c1 = (perm.perm[(ti + i2) and 0xff] xor perm.perm[(tj + j2) and 0xff]) and 0b11
    val c2 = (perm.perm[(ti + 1) and 0xff] xor perm.perm[(tj + 1) and 0xff]) and 0b11
    val tileIndex = c0 or (c1 shl 2) or (c2 shl 4)


    for(tiles in fractals.octaves) {
        val tile = tiles[tileIndex]

        val g0 = grads.grads[tile[lj][li].rem(grads.grads.size)]
        val g1 = grads.grads[tile[lj + j1][li + i1].rem(grads.grads.size)]
        val g2 = grads.grads[tile[lj + 1][li + 1].rem(grads.grads.size)]

        noise += amplitude * simplexSimp(x0, y0, i1, j1, g0, g1, g2)
        frequency *= lacunarity
        amplitude *= gain

        lj *= 2
        li *= 2
    }

    return noise
}

fun simplexTiles(grads: Gradients<Vector2>, perm: PermutationTable, tile: TriangleTile, x: Float, y: Float): Float {
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val xs = x + s
    val ys = y + s
    val i = ffloor(xs)
    val j = ffloor(ys)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    var x0 = x - X0
    var y0 = y - Y0


    var i1 = 0
    var j1 = 0
    if(x0 >= y0) { // bottom triangle
        i1 = 1
        j1 = 0
    } else { // top triangle
        i1 = 0
        j1 = 1
    }

    val li = i and 0x3
    val lj = j and 0x3

    val isObjectSpaceBottomTile =
            if(li == lj) x0 >= y0
            else li > lj
    val isTileSpaceBottomTile = tile.bottom

    var i1p = i1
    var j1p = j1

    val (tlx, tly) =
        if(isObjectSpaceBottomTile xor isTileSpaceBottomTile) {
            i1p = j1
            j1p = i1

            /*val t = x0
            x0 = y0
            y0 = t*/

            Pair(lj, li)
        }
        else Pair(li, lj)

    val g0 = grads.grads[tile[tly][tlx].rem(grads.grads.size)]
    val g1 = grads.grads[tile[tly + j1p][tlx + i1p].rem(grads.grads.size)]
    val g2 = grads.grads[tile[tly + 1][tlx + 1].rem(grads.grads.size)]


    val x1 = x0 - i1p + G2
    val y1 = y0 - j1p + G2
    val x2 = x0 - 1f + 2f * G2
    val y2 = y0 - 1f + 2f * G2

    val t0 = 0.5f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 0.5f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 0.5f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    return 70f * (n0 + n1 + n2)
}

fun perlinSimplex(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xf = ffloor(x)
    val yf = ffloor(y)

    val xs = x - xf
    val ys = y - yf

    val xs1 = xs - 1
    val ys1 = ys - 1

    val g00 = g(grads, perm, xf, yf)
    val g01 = g(grads, perm, xf, yf + 1)
    val g10 = g(grads, perm, xf + 1, yf)
    val g11 = g(grads, perm, xf + 1, yf + 1)

    val t00 = 1f - xs * xs - ys * ys
    val n00 =
        if(t00 < 0) 0f
        else {
            val t00p = t00 * t00
            t00p * t00p * dot(g00, xs, ys)
        }

    val t01 = 1f - xs * xs - ys1 * ys1
    val n01 =
        if(t01 < 0) 0f
        else {
            val t01p = t01 * t01
            t01p * t01p * dot(g01, xs, ys1)
        }

    val t10 = 1f - xs1 * xs1 - ys * ys
    val n10 =
        if(t10 < 0) 0f
        else {
            val t10p = t10 * t10
            t10p * t10p * dot(g10, xs1, ys)
        }

    val t11 = 1f - xs1 * xs1 - ys1 * ys1
    val n11 =
        if(t11 < 0) 0f
        else {
            val t11p = t11 * t11
            t11p * t11p * dot(g11, xs1, ys1)
        }

    return 3.1f * (n00 + n01 + n10 + n11)
}

/*fun perlinSimplex(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xf = ffloor(x)
    val yf = ffloor(y)

    val xs = x - xf
    val ys = y - yf

    val xs1 = xs - 1
    val ys1 = ys - 1

    val g00 = g(grads, perm, xf, yf)
    val g01 = g(grads, perm, xf, yf + 1)
    val g10 = g(grads, perm, xf + 1, yf)
    val g11 = g(grads, perm, xf + 1, yf + 1)

    val t00 = 1f - xs * xs - ys * ys
    val n00 =
        if(t00 < 0) 0f
        else {
            val t00p = t00 * t00
            t00p * t00p * dot(g00, xs, ys)
        }

    val t01 = 1f - xs * xs - ys1 * ys1
    val n01 =
        if(t01 < 0) 0f
        else {
            val t01p = t01 * t01
            t01p * t01p * dot(g01, xs, ys1)
        }

    val t10 = 1f - xs1 * xs1 - ys * ys
    val n10 =
        if(t10 < 0) 0f
        else {
            val t10p = t10 * t10
            t10p * t10p * dot(g10, xs1, ys)
        }

    val t11 = 1f - xs1 * xs1 - ys1 * ys1
    val n11 =
        if(t11 < 0) 0f
        else {
            val t11p = t11 * t11
            t11p * t11p * dot(g11, xs1, ys1)
        }

    return 3.1f * (n00 + n01 + n10 + n11)
}*/
fun fabs(f: Float): Float {
    if(f >= 0f) return f
    else return -f
}

fun simplex(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    var i1 = 0
    var j1 = 0
    if(x0 > y0) {
        i1 = 1
        j1 = 0
    } else {
        i1 = 0
        j1 = 1
    }

    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x0 - 1f + 2f * G2
    val y2 = y0 - 1f + 2f * G2

    val g0 = g(grads, perm, i, j)
    val g1 = g(grads, perm, i + i1, j + j1)
    val g2 = g(grads, perm, i + 1, j + 1)

    val t0 = 0.5f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 0.5f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 0.5f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    return 99.2043119749f * (n0 + n1 + n2)
}

/*fun simplex2(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    var i1 = 0
    var j1 = 0
    if(x0 > y0) {
        //println("bottom triangle")
        i1 = 1
        j1 = 0
    } else {
        //println("top triangle")
        i1 = 0
        j1 = 1
    }

    *//*val (x0, y0) = Pair(0f, 0f)
    println(Pair(x0, y0))
    val (x1, y1) = Pair(x0 - i1 + G2, y0 - j1 + G2)
    println(skew(x1, y1))
    val (x2, y2) = Pair(x1 - i1 + G2, y1 - j1 + G2)
    println(skew(x2, y2))
    val (x3, y3) = Pair(x0 + j1 - G2, y0 + i1 - G2)
    println(skew(x3, y3))
    val (x4, y4) = Pair(x1 + j1 - G2, y1 + i1 - G2)
    println(skew(x4, y4))
    val (x5, y5) = Pair(x0 + i1 - G2, y0 + j1 - G2)
    println(skew(x5, y5))
    val (x6, y6) = Pair(x0 - j1 + G2, y0 - i1 + G2)
    println(skew(x6, y6))
    val (x7, y7) = Pair(x0 + 1f - 2f * G2, y0 + 1f - 2f * G2)
    println(skew(x7, y7))
    val (x8, y8) = Pair(x0 - 1f + 2f * G2, y0 - 1f + 2f * G2)
    println(skew(x8, y8))
    val (x9, y9) = Pair(x8 - i1 + G2, y8 - j1 + G2)
    println(skew(x9, y9))
    val (x10, y10) = Pair(x8 - j1 + G2, y8 - i1 + G2)
    println(skew(x10, y10))
    val (x11, y11) = Pair(x8 - 1f + 2f * G2, y8 - 1f + 2f * G2)
    println(skew(x11, y11))*//*

    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x1 - i1 + G2
    val y2 = y1 - j1 + G2
    val x3 = x0 + j1 - G2
    val y3 = y0 + i1 - G2
    val x4 = x1 + j1 - G2
    val y4 = y1 + i1 - G2
    val x5 = x0 + i1 - G2
    val y5 = y0 + j1 - G2
    val x6 = x0 - j1 + G2
    val y6 = y0 - i1 + G2
    val x7 = x0 + 1f - 2f * G2
    val y7 = y0 + 1f - 2f * G2
    val x8 = x0 - 1f + 2f * G2
    val y8 = y0 - 1f + 2f * G2
    val x9 = x8 - i1 + G2
    val y9 = y8 - j1 + G2
    val x10 = x8 - j1 + G2
    val y10 = y8 - i1 + G2
    val x11 = x8 - 1f + 2f * G2
    val y11 = y8 - 1f + 2f * G2

    val gx0 = i
    val gy0 = j
    val gx1 = gx0 + i1
    val gy1 = gy0 + j1
    val gx2 = gx1 + i1
    val gy2 = gy1 + j1
    val gx3 = gx0 - j1
    val gy3 = gy0 - i1
    val gx4 = gx1 - j1
    val gy4 = gy1 - i1
    val gx5 = gx0 - i1
    val gy5 = gy0 - j1
    val gx6 = gx0 + j1
    val gy6 = gy0 + i1
    val gx7 = gx0 - 1
    val gy7 = gy0 - 1
    val gx8 = gx0 + 1
    val gy8 = gy0 + 1
    val gx9 = gx8 + i1
    val gy9 = gy8 + j1
    val gx10 = gx8 + j1
    val gy10 = gx8 + i1
    val gx11 = gx8 + 1
    val gy11 = gx8 + 1

    *//*println("s: $s")
    println("(i, j): " + Pair(i, j))
    println("(x0, y0): " + Pair(gx0, gy0))
    println("(x1, y1): " + Pair(gx1, gy1))
    println("(x2, y2): " + Pair(gx2, gy2))
    println("(x3, y3): " + Pair(gx3, gy3))
    println("(x4, y4): " + Pair(gx4, gy4))
    println("(x5, y5): " + Pair(gx5, gy5))
    println("(x6, y6): " + Pair(gx6, gy6))
    println("(x7, y7): " + Pair(gx7, gy7))
    println("(x8, y8): " + Pair(gx8, gy8))
    println("(x9, y9): " + Pair(gx9, gy9))
    println("(x10, y10): " + Pair(gx10, gy10))
    println("(x11, y11): " + Pair(gx11, gy11))*//*

    val g0 = g(grads, perm, gx0, gy0)
    val g1 = g(grads, perm, gx1, gy1)
    val g2 = g(grads, perm, gx2, gy2)
    val g3 = g(grads, perm, gx3, gy3)
    val g4 = g(grads, perm, gx4, gy4)
    val g5 = g(grads, perm, gx5, gy5)
    val g6 = g(grads, perm, gx6, gy6)
    val g7 = g(grads, perm, gx7, gy7)
    val g8 = g(grads, perm, gx8, gy8)
    val g9 = g(grads, perm, gx9, gy9)
    val g10 = g(grads, perm, gx10, gy10)
    val g11 = g(grads, perm, gx11, gy11)

    val t0 = 1f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 1f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 1f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    val t3 = 1f - x3 * x3 - y3 * y3
    val n3 =
        if(t3 < 0) 0f
        else {
            val t3p = t3 * t3
            t3p * t3p * dot(g3, x3, y3)
        }

    val t4 = 1f - x4 * x4 - y4 * y4
    val n4 =
        if(t4 < 0) 0f
        else {
            val t4p = t4 * t4
            t4p * t4p * dot(g4, x4, y4)
        }

    val t5 = 1f - x5 * x5 - y5 * y5
    val n5 =
        if(t5 < 0) 0f
        else {
            val t5p = t5 * t5
            t5p * t5p * dot(g5, x5, y5)
        }

    val t6 = 1f - x6 * x6 - y6 * y6
    val n6 =
        if(t6 < 0) 0f
        else {
            val t6p = t6 * t6
            t6p * t6p * dot(g6, x6, y6)
        }

    val t7 = 1f - x7 * x7 - y7 * y7
    val n7 =
        if(t7 < 0) 0f
        else {
            val t7p = t7 * t7
            t7p * t7p * dot(g7, x7, y7)
        }

    val t8 = 1f - x8 * x8 - y8 * y8
    val n8 =
        if(t8 < 0) 0f
        else {
            val t8p = t8 * t8
            t8p * t8p * dot(g8, x8, y8)
        }

    val t9 = 1f - x9 * x9 - y9 * y9
    val n9 =
        if(t9 < 0) 0f
        else {
            val t9p = t9 * t9
            t9p * t9p * dot(g9, x9, y9)
        }

    val t10 = 1f - x10 * x10 - y10 * y10
    val n10 =
        if(t10 < 0) 0f
        else {
            val t10p = t10 * t10
            t10p * t10p * dot(g10, x10, y10)
        }

    val t11 = 1f - x11 * x11 - y11 * y11
    val n11 =
        if(t11 < 0) 0f
        else {
            val t11p = t11 * t11
            t11p * t11p * dot(g11, x11, y11)
        }

    return 1.96561123826f * (n0 + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + n10 + n11)
}*/

fun simplex2(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float, contrib: Int): Float {
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    var i1 = 0
    var j1 = 0
    if(x0 > y0) {
        //println("bottom triangle")
        i1 = 1
        j1 = 0
    } else {
        //println("top triangle")
        i1 = 0
        j1 = 1
    }

    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x1 - i1 + G2
    val y2 = y1 - j1 + G2
    val x3 = x0 + j1 - G2
    val y3 = y0 + i1 - G2
    val x4 = x1 + j1 - G2
    val y4 = y1 + i1 - G2
    val x5 = x0 + i1 - G2
    val y5 = y0 + j1 - G2
    val x6 = x0 - j1 + G2
    val y6 = y0 - i1 + G2
    val x7 = x0 + 1f - 2f * G2
    val y7 = y0 + 1f - 2f * G2
    val x8 = x1 - j1 + G2
    val y8 = y1 - i1 + G2
    /*val x8v = x0 - 1f + 2f * G2
    val y8v = y0 - 1f + 2f * G2*/
    //println("${Pair(x8, y8)} ${Pair(x8v, y8v)}")
    val x9 = x8 - 1 + G2
    val y9 = y8 + G2
    val x10 = x8 + G2
    val y10 = y8 - 1 + G2
    val x11 = x8 - 1f + 2f * G2
    val y11 = y8 - 1f + 2f * G2

    val gx0 = i
    val gy0 = j
    val gx1 = gx0 + i1
    val gy1 = gy0 + j1
    val gx2 = gx1 + i1
    val gy2 = gy1 + j1
    val gx3 = gx0 - j1
    val gy3 = gy0 - i1
    val gx4 = gx1 - j1
    val gy4 = gy1 - i1
    val gx5 = gx0 - i1
    val gy5 = gy0 - j1
    val gx6 = gx0 + j1
    val gy6 = gy0 + i1
    val gx7 = gx0 - 1
    val gy7 = gy0 - 1
    val gx8 = gx0 + 1
    val gy8 = gy0 + 1
    val gx9 = gx8 + 1
    val gy9 = gy8
    val gx10 = gx8
    val gy10 = gx8 + 1
    val gx11 = gx8 + 1
    val gy11 = gx8 + 1



    val g0 = g(grads, perm, gx0, gy0)
    val g1 = g(grads, perm, gx1, gy1)
    val g2 = g(grads, perm, gx2, gy2)
    val g3 = g(grads, perm, gx3, gy3)
    val g4 = g(grads, perm, gx4, gy4)
    val g5 = g(grads, perm, gx5, gy5)
    val g6 = g(grads, perm, gx6, gy6)
    val g7 = g(grads, perm, gx7, gy7)
    val g8 = g(grads, perm, gx8, gy8)
    val g9 = g(grads, perm, gx9, gy9)
    val g10 = g(grads, perm, gx10, gy10)
    val g11 = g(grads, perm, gx11, gy11)

    val t0 = 1.2f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 1.2f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 1.2f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    val t3 = 1.2f - x3 * x3 - y3 * y3
    val n3 =
        if(t3 < 0) 0f
        else {
            val t3p = t3 * t3
            t3p * t3p * dot(g3, x3, y3)
        }

    val t4 = 1.2f - x4 * x4 - y4 * y4
    val n4 =
        if(t4 < 0) 0f
        else {
            val t4p = t4 * t4
            t4p * t4p * dot(g4, x4, y4)
        }

    val t5 = 1.2f - x5 * x5 - y5 * y5
    val n5 =
        if(t5 < 0) 0f
        else {
            val t5p = t5 * t5
            t5p * t5p * dot(g5, x5, y5)
        }

    val t6 = 1.2f - x6 * x6 - y6 * y6
    val n6 =
        if(t6 < 0) 0f
        else {
            val t6p = t6 * t6
            t6p * t6p * dot(g6, x6, y6)
        }

    val t7 = 1.2f - x7 * x7 - y7 * y7
    val n7 =
        if(t7 < 0) 0f
        else {
            val t7p = t7 * t7
            t7p * t7p * dot(g7, x7, y7)
        }

    val t8 = 1.2f - x8 * x8 - y8 * y8
    val n8 =
        if(t8 < 0) 0f
        else {
            val t8p = t8 * t8
            t8p * t8p * dot(g8, x8, y8)
        }

    val t9 = 1.2f - x9 * x9 - y9 * y9
    val n9 =
        if(t9 < 0) 0f
        else {
            val t9p = t9 * t9
            t9p * t9p * dot(g9, x9, y9)
        }

    val t10 = 1.2f - x10 * x10 - y10 * y10
    val n10 =
        if(t10 < 0) 0f
        else {
            val t10p = t10 * t10
            t10p * t10p * dot(g10, x10, y10)
        }

    val t11 = 1.2f - x11 * x11 - y11 * y11
    val n11 =
        if(t11 < 0) 0f
        else {
            val t11p = t11 * t11
            t11p * t11p * dot(g11, x11, y11)
        }

    //return 70f * (n0 + n1 + n8)

    /*println("s: $s")
    println("(i, j): " + Pair(i, j))
    println("(x0, y0): " + Pair(gx0, gy0) + Pair(x0, y0) + skew(x0, y0))
    println("(x1, y1): " + Pair(gx1, gy1) + Pair(x1, y1) + skew(x1, y1))
    println("(x2, y2): " + Pair(gx2, gy2) + Pair(x2, y2) + skew(x2, y2))
    println("(x3, y3): " + Pair(gx3, gy3) + Pair(x3, y3) + skew(x3, y3))
    println("(x4, y4): " + Pair(gx4, gy4) + Pair(x4, y4) + skew(x4, y4))
    println("(x5, y5): " + Pair(gx5, gy5) + Pair(x5, y5) + skew(x5, y5))
    println("(x6, y6): " + Pair(gx6, gy6) + Pair(x6, y6) + skew(x6, y6))
    println("(x7, y7): " + Pair(gx7, gy7) + Pair(x7, y7) + skew(x7, y7))
    println("(x8, y8): " + Pair(gx8, gy8) + Pair(x8, y8) + skew(x8, y8))
    println("(x9, y9): " + Pair(gx9, gy9) + Pair(x9, y9) + skew(x9, y9))
    println("(x10, y10): " + Pair(gx10, gy10) + Pair(x10, y10) + skew(x10, y10))
    println("(x11, y11): " + Pair(gx11, gy11) + Pair(x11, y11) + skew(x11, y11))*/

    //return /*1.96561123826f * */ 0.77247952098f * (n0 + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + n10 + n11)

    return when (contrib) {
                0 -> n0
                1 -> n1
                2 -> n2
                3 -> n3
                4 -> n4
                5 -> n5
                6 -> n6
                7 -> n7
                8 -> n8
                9 -> n9
                10 -> n10
                11 -> n11
                else -> 0.77247952098f * (n0 + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + n10 + n11)
            } * 3
}

fun simplex3(grads: Gradients<Vector2>, perm: PermutationTable, x1: Float, y1: Float): Float {
    val x = x1 * 1.46875f
    val y = y1 * 1.46875f
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    val gx0 = i
    val gy0 = j

    val gx1 = gx0 + 1
    val gy1 = gy0
    val x1 = x0 - 1f + G2
    val y1 = y0 + G2

    val gx2 = gx0
    val gy2 = gy0 + 1
    val x2 = x0 + G2
    val y2 = y0 - 1f + G2

    val gx3 = gx0 - 1
    val gy3 = gy0
    val x3 = x0 + 1f - G2
    val y3 = y0 - G2

    val gx4 = gx0
    val gy4 = gy0 - 1
    val x4 = x0 - G2
    val y4 = y0 + 1f - G2

    val gx5 = gx3
    val gy5 = gy4
    val x5 = x3 - G2
    val y5 = y4 - G2

    val gx6 = gx1
    val gy6 = gy2
    val x6 = x1 + G2
    val y6 = y2 + G2

    val gx7 = gx6 + 1
    val gy7 = gy6
    val x7 = x6 - 1f + G2
    val y7 = y6 + G2

    val gx8 = gx6
    val gy8 = gy6 + 1
    val x8 = x6 + G2
    val y8 = y6 - 1f + G2

    val gx9 = gx7
    val gy9 = gy8
    val x9 = x7 + G2
    val y9 = y8 + G2

    var gx10 = gx1
    var gy10 = gy4
    var x10 = x1 - G2
    var y10 = y4 + G2

    var gx11 = gx7
    var gy11 = gy1
    var x11 = x7 - G2
    var y11 = y1 + G2

    if(x0 < y0) {
        gx10 = gx3
        gy10 = gy2
        x10 = x3 + G2
        y10 = y2 - G2

        gx11 = gx2
        gy11 = gy8
        x11 = x2 + G2
        y11 = y8 - G2
    }

    val g0 = g(grads, perm, gx0, gy0)
    val g1 = g(grads, perm, gx1, gy1)
    val g2 = g(grads, perm, gx2, gy2)
    val g3 = g(grads, perm, gx3, gy3)
    val g4 = g(grads, perm, gx4, gy4)
    val g5 = g(grads, perm, gx5, gy5)
    val g6 = g(grads, perm, gx6, gy6)
    val g7 = g(grads, perm, gx7, gy7)
    val g8 = g(grads, perm, gx8, gy8)
    val g9 = g(grads, perm, gx9, gy9)
    val g10 = g(grads, perm, gx10, gy10)
    val g11 = g(grads, perm, gx11, gy11)

    val t0 = 1.4f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 1.4f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 1.4f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    val t3 = 1.4f - x3 * x3 - y3 * y3
    val n3 =
        if(t3 < 0) 0f
        else {
            val t3p = t3 * t3
            t3p * t3p * dot(g3, x3, y3)
        }

    val t4 = 1.4f - x4 * x4 - y4 * y4
    val n4 =
        if(t4 < 0) 0f
        else {
            val t4p = t4 * t4
            t4p * t4p * dot(g4, x4, y4)
        }

    val t5 = 1.4f - x5 * x5 - y5 * y5
    val n5 =
        if(t5 < 0) 0f
        else {
            val t5p = t5 * t5
            t5p * t5p * dot(g5, x5, y5)
        }

    val t6 = 1.4f - x6 * x6 - y6 * y6
    val n6 =
        if(t6 < 0) 0f
        else {
            val t6p = t6 * t6
            t6p * t6p * dot(g6, x6, y6)
        }

    val t7 = 1.4f - x7 * x7 - y7 * y7
    val n7 =
        if(t7 < 0) 0f
        else {
            val t7p = t7 * t7
            t7p * t7p * dot(g7, x7, y7)
        }

    val t8 = 1.4f - x8 * x8 - y8 * y8
    val n8 =
        if(t8 < 0) 0f
        else {
            val t8p = t8 * t8
            t8p * t8p * dot(g8, x8, y8)
        }

    val t9 = 1.4f - x9 * x9 - y9 * y9
    val n9 =
        if(t9 < 0) 0f
        else {
            val t9p = t9 * t9
            t9p * t9p * dot(g9, x9, y9)
        }

    val t10 = 1.4f - x10 * x10 - y10 * y10
    val n10 =
        if(t10 < 0) 0f
        else {
            val t10p = t10 * t10
            t10p * t10p * dot(g10, x10, y10)
        }

    val t11 = 1.4f - x11 * x11 - y11 * y11
    val n11 =
        if(t11 < 0) 0f
        else {
            val t11p = t11 * t11
            t11p * t11p * dot(g11, x11, y11)
        }

    return (n0 + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + n10 + n11) * 0.3504393651f // * 0.77358899108f
}

fun simplex3v2(grads: Gradients<Vector2>, perm: PermutationTable, x1: Float, y1: Float): Float {
    val x = x1 * 1.46875f
    val y = y1 * 1.46875f
    val F2 = 0.5f * (sqrt(3f)-1f)
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val G2 = (3f - sqrt(3f))/6f
    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    val gx0 = i
    val gy0 = j

    val gx1 = gx0 + 1
    val gy1 = gy0
    val x1 = x0 - 1f + G2
    val y1 = y0 + G2

    val gx2 = gx0
    val gy2 = gy0 + 1
    val x2 = x0 + G2
    val y2 = y0 - 1f + G2

    val gx3 = gx0 - 1
    val gy3 = gy0
    val x3 = x0 + 1f - G2
    val y3 = y0 - G2

    val gx4 = gx0
    val gy4 = gy0 - 1
    val x4 = x0 - G2
    val y4 = y0 + 1f - G2

    val gx5 = gx3
    val gy5 = gy4
    val x5 = x3 - G2
    val y5 = y4 - G2

    val gx6 = gx1
    val gy6 = gy2
    val x6 = x1 + G2
    val y6 = y2 + G2

    val gx7 = gx6 + 1
    val gy7 = gy6
    val x7 = x6 - 1f + G2
    val y7 = y6 + G2

    val gx8 = gx6
    val gy8 = gy6 + 1
    val x8 = x6 + G2
    val y8 = y6 - 1f + G2

    val gx9 = gx7
    val gy9 = gy8
    val x9 = x7 + G2
    val y9 = y8 + G2

    var gx10 = gx1
    var gy10 = gy4
    var x10 = x1 - G2
    var y10 = y4 + G2

    var gx11 = gx7
    var gy11 = gy1
    var x11 = x7 - G2
    var y11 = y1 + G2

    if(x0 < y0) {
        gx10 = gx3
        gy10 = gy2
        x10 = x3 + G2
        y10 = y2 - G2

        gx11 = gx2
        gy11 = gy8
        x11 = x2 + G2
        y11 = y8 - G2
    }

    val g0 = g(grads, perm, gx0, gy0)
    val g1 = g(grads, perm, gx1, gy1)
    val g2 = g(grads, perm, gx2, gy2)
    val g3 = g(grads, perm, gx3, gy3)
    val g4 = g(grads, perm, gx4, gy4)
    val g5 = g(grads, perm, gx5, gy5)
    val g6 = g(grads, perm, gx6, gy6)
    val g7 = g(grads, perm, gx7, gy7)
    val g8 = g(grads, perm, gx8, gy8)
    val g9 = g(grads, perm, gx9, gy9)
    val g10 = g(grads, perm, gx10, gy10)
    val g11 = g(grads, perm, gx11, gy11)

    val t0 = 1.2f - x0 * x0 - y0 * y0
    val n0 =
        if(t0 < 0) 0f
        else {
            val t0p = t0 * t0
            t0p * t0p * dot(g0, x0, y0)
        }

    val t1 = 1.2f - x1 * x1 - y1 * y1
    val n1 =
        if(t1 < 0) 0f
        else {
            val t1p = t1 * t1
            t1p * t1p * dot(g1, x1, y1)
        }

    val t2 = 1.2f - x2 * x2 - y2 * y2
    val n2 =
        if(t2 < 0) 0f
        else {
            val t2p = t2 * t2
            t2p * t2p * dot(g2, x2, y2)
        }

    val t3 = 1.2f - x3 * x3 - y3 * y3
    val n3 =
        if(t3 < 0) 0f
        else {
            val t3p = t3 * t3
            t3p * t3p * dot(g3, x3, y3)
        }

    val t4 = 1.2f - x4 * x4 - y4 * y4
    val n4 =
        if(t4 < 0) 0f
        else {
            val t4p = t4 * t4
            t4p * t4p * dot(g4, x4, y4)
        }

    val t5 = 1.2f - x5 * x5 - y5 * y5
    val n5 =
        if(t5 < 0) 0f
        else {
            val t5p = t5 * t5
            t5p * t5p * dot(g5, x5, y5)
        }

    val t6 = 1.2f - x6 * x6 - y6 * y6
    val n6 =
        if(t6 < 0) 0f
        else {
            val t6p = t6 * t6
            t6p * t6p * dot(g6, x6, y6)
        }

    val t7 = 1.2f - x7 * x7 - y7 * y7
    val n7 =
        if(t7 < 0) 0f
        else {
            val t7p = t7 * t7
            t7p * t7p * dot(g7, x7, y7)
        }

    val t8 = 1.2f - x8 * x8 - y8 * y8
    val n8 =
        if(t8 < 0) 0f
        else {
            val t8p = t8 * t8
            t8p * t8p * dot(g8, x8, y8)
        }

    val t9 = 1.2f - x9 * x9 - y9 * y9
    val n9 =
        if(t9 < 0) 0f
        else {
            val t9p = t9 * t9
            t9p * t9p * dot(g9, x9, y9)
        }

    val t10 = 1.2f - x10 * x10 - y10 * y10
    val n10 =
        if(t10 < 0) 0f
        else {
            val t10p = t10 * t10
            t10p * t10p * dot(g10, x10, y10)
        }

    val t11 = 1.2f - x11 * x11 - y11 * y11
    val n11 =
        if(t11 < 0) 0f
        else {
            val t11p = t11 * t11
            t11p * t11p * dot(g11, x11, y11)
        }

    return (n0 + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + n10 + n11) * 0.77358899108f
}

fun perlinReduced(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xf = ffloor(x)

    val xs = x - xf

    val xs1 = xs - 1

    val u = interp(xs)

    val u1 = 1 - u

    val xsu1 = xs * u1
    val xs1u = xs1 * u

    val g0 = g(grads, perm, xf)
    val g1 = g(grads, perm, xf + 1)

    return dot(g0, xsu1) + dot(g1, xs1u)
}

fun perlinReducedAmp(grads: Gradients<Vector1>, perm: PermutationTable, x: Float, a1: Float, a2: Float): Float {
    val xf = ffloor(x)

    val xs = x - xf

    val xs1 = xs - 1

    val u = interp(xs)

    val u1 = 1 - u

    val xsu1 = xs * u1 * a1
    val xs1u = xs1 * u * a2

    val g0 = g(grads, perm, xf)
    val g1 = g(grads, perm, xf + 1)

    return dot(g0, xsu1) + dot(g1, xs1u)
}

fun fractalAmp(grads: Gradients<Vector1>, perm: PermutationTable, x: Float, octaves: Int): Float {
    var noise = 0f

    val lacunarity = 2.48779f//.12791f
    val gain = 0.5f

    var amplitude = 1f
    var frequency = 1f

    var max = 0f

    for (i in 0 until octaves) {
        max += amplitude
        val n = perlinReducedAmp(grads, perm, frequency * x, amplitude, amplitude)
        noise += n
        frequency *= lacunarity
        amplitude *= gain
    }

    return noise / max
}

fun fractal(grads: Gradients<Vector1>, perm: PermutationTable, x: Float, octaves: Int): Float {
    var noise = 0f

    val lacunarity = 2f
    val gain = 0.5f

    var amplitude = 1f
    var frequency = 1f

    var max = 0f

    for (i in 0 until octaves) {
        max += amplitude
        val n = amplitude * perlin(grads, perm, frequency * x)
        noise += n
        /*val n0 = perlin(grads, perm, frequency * x)
        val n1 = perlin(grads, perm, frequency * (x + 0.3f))
        val n2 = perlin(grads, perm, frequency * (x + 0.6f))
        noise += amplitude * ((n0/2f) + (n1/4f) + (n2/4f)) * 3f*/
        frequency *= lacunarity
        amplitude *= gain
    }

    return noise / max
}

fun perlin(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xf = ffloor(x)

    val xs = x - xf

    val g0 = g(grads, perm, xf)
    val g1 = g(grads, perm, xf + 1)

    val n0 = dot(g0, xs)
    val n1 = dot(g1, xs - 1)

    val u = interp(xs)

    return lerp(n0, n1, u) * 2.05214761557f
}

fun perlinRadial2v2(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xp = x * 2.09375f
    val yp = y * 2.09375f

    val xf = ffloor(xp)
    val yf = ffloor(yp)
    val xs = xp - xf
    val ys = yp - yf

    var n = 0f

    for(i in -1 until 3) {
        for(j in -1 until 3) {
            val g = g(grads, perm, xf + i, yf + j)
            val xsi = xs - i
            val ysj = ys - j
            val t = 2f - xsi * xsi - ysj * ysj
            n +=
                if(t < 0) 0f
                else {
                    val tp = t * t
                    //val tpp = tp * tp
                    /*tp * */tp * dot(g, xsi, ysj)
                }
        }
    }

    return n * 0.15713484842f // * 0.0698377059f// * 0.16271393832f
}

fun perlinRadial2(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xp = 1.5f * x
    val yp = 1.5f * y

    val xf = ffloor(xp)
    val yf = ffloor(yp)
    val xs = xp - xf
    val ys = yp - yf

    var n = 0f

    for(i in -1 until 3) {
        for(j in -1 until 3) {
            val g = g(grads, perm, xf + i, yf + j)
            val xsi = xs - i
            val ysj = ys - j
            val t = 2f - xsi * xsi - ysj * ysj
            n +=
                if(t < 0) 0f
                else {
                    val tp = t * t
                    //val tpp = tp * tp
                    tp * tp * dot(g, xsi, ysj)
                }
        }
    }

    return n * 0.0698377059f// * 0.16271393832f
}

fun perlinRadial3(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xp = 1.5f * x
    val yp = 1.5f * y

    val xf = ffloor(xp)
    val yf = ffloor(yp)
    val xs = xp - xf
    val ys = yp - yf

    var n = 0f

    for(i in -2 until 4) {
        for(j in -2 until 4) {
            val g = g(grads, perm, xf + i, yf + j)
            val xsi = xs - i
            val ysj = ys - j
            val t = 3f - xsi * xsi - ysj * ysj
            n +=
                if(t < 0) 0f
                else {
                    val tp = t * t
                    val tpp = tp * tp
                    tp * tp * dot(g, xsi, ysj)
                }
        }
    }

    return n * 0.00907284917f// * 0.0259184091f
}

fun perlinRadial4(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xp = 1.5f * x
    val yp = 1.5f * y

    val xf = ffloor(xp)
    val yf = ffloor(yp)
    val xs = xp - xf
    val ys = yp - yf

    var n = 0f

    for(i in -3 until 5) {
        for(j in -3 until 5) {
            val g = g(grads, perm, xf + i, yf + j)
            val xsi = xs - i
            val ysj = ys - j
            val t = 4f - xsi * xsi - ysj * ysj
            n +=
                if(t < 0) 0f
                else {
                    val tp = t * t
                    val tpp = tp * tp
                    tp * tp * dot(g, xsi, ysj)
                }
        }
    }

    return n * 0.00215499253f// * 0.00004922521f
}

fun perlinRadial2(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xp = x * 2f
    val xf = ffloor(xp)
    val xs = xp - xf

    var n = 0f

    for(i in -1 until 3) {
        val g = g(grads, perm, xf + i)
        val xsi = xs - i
        val t = 2f - xsi * xsi
        n +=
            if(t < 0) 0f
            else {
                val tp = t * t
                tp * tp * dot(g, xsi)
            }
    }

    return n * 0.11284236373f //* 0.13464010531f
}

fun perlinRadial2v2(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xp = x * 2.09375f
    val xf = ffloor(xp)
    val xs = xp - xf

    var n = 0f

    for(i in -1 until 3) {
        val g = g(grads, perm, xf + i)
        val xsi = xs - i
        val t = 2f - xsi * xsi
        n +=
            if(t < 0) 0f
            else {
                val tp = t * t
                tp * dot(g, xsi)
            }
    }

    return n * 0.3323288141f
}

fun perlinRadial2v3(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xp = x
    val xf = ffloor(xp)
    val xs = xp - xf

    var n = 0f

    for(i in -1 until 3) {
        val g = g(grads, perm, xf + i)
        val xsi = xs - i
        val t = 2f - xsi * xsi
        n +=
            if(t < 0) 0f
            else {
                //val tp = t * t
                t * dot(g, xsi)
            }
    }

    return n * 0.48411326643f
}

fun perlinRadial3(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xp = x
    val xf = ffloor(xp)
    val xs = xp - xf

    var n = 0f

    for(i in -2 until 4) {
        val g = g(grads, perm, xf + i)
        val xsi = xs - i
        val t = 3f - xsi * xsi
        n +=
            if(t < 0) 0f
            else {
                val tp = t * t
                tp * tp * dot(g, xsi)
            }
    }

    return n * 0.01813289135f
}

fun perlinRadial4(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xp = x
    val xf = ffloor(xp)
    val xs = xp - xf

    var n = 0f

    for(i in -3 until 5) {
        val g = g(grads, perm, xf + i)
        val xsi = xs - i
        val t = 4f - xsi * xsi
        n +=
            if(t < 0) 0f
            else {
                val tp = t * t
                tp * tp * dot(g, xsi)
            }
    }

    return n * 0.00483613155f
}

fun perlinReduced(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    //this is the perlin algorithm rewritten to express it as the sum of constants * gradients
    val xf = ffloor(x)
    val yf = ffloor(y)

    val xs = x - xf
    val ys = y - yf

    val xs1 = xs - 1
    val ys1 = ys - 1

    val u = interp(xs)
    val v = interp(ys)

    val u1 = 1 - u
    val v1 = 1 - v

    val xsu1 = xs * u1
    val ysu1 = ys * u1

    val xs1u = xs1 * u
    val ysu = ys * u

    val ys1u1 = ys1 * u1

    val ys1u = ys1 * u

    val xsu1v1 = xsu1 * v1 //c0
    val ysu1v1 = ysu1 * v1

    val xs1uv1 = xs1u * v1 //c2
    val ysuv1 = ysu * v1

    val xsu1v = xsu1 * v //c1
    val ys1u1v = ys1u1 * v

    val xs1uv = xs1u * v //c3
    val ys1uv = ys1u * v

    val g00 = g(grads, perm, xf, yf)
    val g01 = g(grads, perm, xf, yf + 1)
    val g10 = g(grads, perm, xf + 1, yf)
    val g11 = g(grads, perm, xf + 1, yf + 1)

    return (dot(g00, xsu1v1, ysu1v1) + dot(g01, xsu1v, ys1u1v) + dot(g10, xs1uv1, ysuv1) + dot(g11, xs1uv, ys1uv)) * r2
}

fun perlinFractalTiles(grads: Gradients<Vector2>, perm: PermutationTable, fractals: FractalTile, x: Float, y: Float): Float {
    var noise = 0f

    val lacunarity = 2f
    val gain = 0.5f

    var amplitude = 1f
    var frequency = 1f

    var x1 = x
    var y1 = y

    var xf = ffloor(x1)
    var yf = ffloor(y1)

    var xl = xf and 0b11
    var yl = yf and 0b11

    val xt = xf shr 2
    val yt = yf shr 2

    val c0 = perm.perm[xt and 0xff] xor perm.perm[yt and 0xff] and 0b11
    val c1 = perm.perm[(xt + 1) and 0xff] xor perm.perm[yt and 0xff] and 0b11
    val c2 = perm.perm[xt and 0xff] xor perm.perm[(yt + 1) and 0xff] and 0b11
    val c3 = perm.perm[(xt + 1) and 0xff] xor perm.perm[(yt + 1) and 0xff] and 0b11
    val tileIndex = c0 or (c1 shl 2) or (c2 shl 4) or (c3 shl 6)

    var div = 0f

    for(tiles in fractals.octaves) {
        val tile = tiles[tileIndex]

        val xs = x1 - xf
        val ys = y1 - yf

        val g00 = grads.grads[tile[yl][xl].rem(grads.grads.size)]
        val g01 = grads.grads[tile[yl + 1][xl].rem(grads.grads.size)]
        val g10 = grads.grads[tile[yl][xl + 1].rem(grads.grads.size)]
        val g11 = grads.grads[tile[yl + 1][xl + 1].rem(grads.grads.size)]

        div += amplitude
        noise += amplitude * perlinSimp(g00, g01, g10, g11, xs, ys)
        frequency *= lacunarity
        amplitude *= gain

        x1 = xs * 2
        y1 = ys * 2

        xf = ffloor(x1)
        yf = ffloor(y1)

        xl = xl * 2 + xf
        yl = yl * 2 + yf
    }

    //println(arrayListOf(div, noise, noise / div))

    return noise / div
}

fun perlinSimp(g00: Vector2, g01: Vector2, g10: Vector2, g11: Vector2, xs: Float, ys: Float): Float {
    val n00 = dot(g00, xs, ys)
    val n01 = dot(g01, xs, ys - 1)
    val n10 = dot(g10, xs - 1, ys)
    val n11 = dot(g11, xs - 1, ys - 1)

    val u = interp(xs)
    val v = interp(ys)

    val nx0 = lerp(n00, n10, u)
    val nx1 = lerp(n01, n11, u)

    val nxy = lerp(nx0, nx1, v)

    return nxy * r2
}

fun perlinTiles(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<SquareTile>, x: Float, y: Float): Float {
    val xf = ffloor(x)
    val yf = ffloor(y)

    val xt = xf.shr(2)
    val yt = yf.shr(2)

    val c0 = perm.perm[xt and 0xff] xor perm.perm[yt and 0xff] and 0b11
    val c1 = perm.perm[(xt + 1) and 0xff] xor perm.perm[yt and 0xff] and 0b11
    val c2 = perm.perm[xt and 0xff] xor perm.perm[(yt + 1) and 0xff] and 0b11
    val c3 = perm.perm[(xt + 1) and 0xff] xor perm.perm[(yt + 1) and 0xff] and 0b11

    val tile = tiles[c0 or (c1 shl 2) or (c2 shl 4) or (c3 shl 6)]

    val xl = xf and 0b11
    val yl = yf and 0b11

    val g00 = grads.grads[tile[yl][xl].rem(grads.grads.size)]
    val g01 = grads.grads[tile[yl + 1][xl].rem(grads.grads.size)]
    val g10 = grads.grads[tile[yl][xl + 1].rem(grads.grads.size)]
    val g11 = grads.grads[tile[yl + 1][xl + 1].rem(grads.grads.size)]

    val xs = x - xf
    val ys = y - yf

    val n00 = dot(g00, xs, ys)
    val n01 = dot(g01, xs, ys - 1)
    val n10 = dot(g10, xs - 1, ys)
    val n11 = dot(g11, xs - 1, ys - 1)

    val u = interp(xs)
    val v = interp(ys)

    val nx0 = lerp(n00, n10, u)
    val nx1 = lerp(n01, n11, u)

    val nxy = lerp(nx0, nx1, v)

    return nxy * r2
}


fun perlin(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xf = ffloor(x)
    val yf = ffloor(y)

    val xs = x - xf
    val ys = y - yf

    val g00 = g(grads, perm, xf, yf)
    val g01 = g(grads, perm, xf, yf + 1)
    val g10 = g(grads, perm, xf + 1, yf)
    val g11 = g(grads, perm, xf + 1, yf + 1)

    val n00 = dot(g00, xs, ys)
    val n01 = dot(g01, xs, ys - 1)
    val n10 = dot(g10, xs - 1, ys)
    val n11 = dot(g11, xs - 1, ys - 1)

    val u = interp(xs)
    val v = interp(ys)

    val nx0 = lerp(n00, n10, u)
    val nx1 = lerp(n01, n11, u)

    val nxy = lerp(nx0, nx1, v)

    return nxy * r2
}

fun perlin(grads: Gradients<Vector3>, perm: PermutationTable, x: Float, y: Float, z: Float): Float {
    val xf = ffloor(x)
    val yf = ffloor(y)
    val zf = ffloor(z)

    val xs = x - xf
    val ys = y - yf
    val zs = z - zf

    val g000 = g(grads, perm, xf, yf, zf)
    val g001 = g(grads, perm, xf, yf, zf + 1)
    val g010 = g(grads, perm, xf, yf + 1, zf)
    val g011 = g(grads, perm, xf, yf + 1, zf + 1)
    val g100 = g(grads, perm, xf + 1, yf, zf)
    val g101 = g(grads, perm, xf + 1, yf, zf + 1)
    val g110 = g(grads, perm, xf + 1, yf + 1, zf)
    val g111 = g(grads, perm, xf + 1, yf + 1, zf + 1)

    val n000 = dot(g000, xs, ys, zs)
    val n001 = dot(g001, xs, ys, zs - 1)
    val n010 = dot(g010, xs, ys - 1, zs)
    val n011 = dot(g011, xs, ys - 1, zs - 1)
    val n100 = dot(g100, xs - 1, ys, zs)
    val n101 = dot(g101, xs - 1, ys, zs - 1)
    val n110 = dot(g110, xs - 1, ys - 1, zs)
    val n111 = dot(g111, xs - 1, ys - 1, zs - 1)

    val u = interp(xs)
    val v = interp(ys)
    val w = interp(zs)

    val nx00 = lerp(n000, n100, u)
    val nx01 = lerp(n001, n101, u)
    val nx10 = lerp(n010, n110, u)
    val nx11 = lerp(n011, n111, u)

    val nxy0 = lerp(nx00, nx10, v)
    val nxy1 = lerp(nx01, nx11, v)

    val nxyz = lerp(nxy0, nxy1, w)

    return nxyz
}