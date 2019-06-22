package rewrite

import kotlin.math.abs

fun myperlin(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xp = x * 2.09375f
    val xf = ffloor(xp)
    val xs = xp - xf

    var n = 0f

    for(i in -1 until 3) {
        val g = grads[perm[xf + i]]
        val xsi = xs - i
        val t = 2f - xsi * xsi
        n +=
            if(t < 0) 0f
            else {
                val tp = t * t
                tp * g.dot(xsi)
            }
    }

    return n * 0.3323288141f
}

fun myperlin(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xp = x * 2.09375f
    val yp = y * 2.09375f

    val xf = ffloor(xp)
    val yf = ffloor(yp)
    val xs = xp - xf
    val ys = yp - yf

    var n = 0f

    for(i in -1 until 3) {
        for(j in -1 until 3) {
            val g = grads[perm[xf + i, yf + j]]
            val xsi = xs - i
            val ysj = ys - j
            val t = 2f - xsi * xsi - ysj * ysj
            n +=
                if(t < 0) 0f
                else {
                    val tp = t * t
                    tp * g.dot(xsi, ysj)
                }
        }
    }

    return n * 0.15713484842f
}

fun myperlin(grads: Gradients<Vector3>, perm: PermutationTable, x: Float, y: Float, z: Float): Float {
    val xp = x * 2.09375f
    val yp = y * 2.09375f
    val zp = z * 2.09375f

    val xf = ffloor(xp)
    val yf = ffloor(yp)
    val zf = ffloor(zp)
    val xs = xp - xf
    val ys = yp - yf
    val zs = zp - zf

    var n = 0f

    for(i in -1 until 3) {
        for(j in -1 until 3) {
            for(k in -1 until 3) {
                val g = grads[perm[xf + i, yf + j, zf + k]]
                val xsi = xs - i
                val ysj = ys - j
                val zsk = zs - k
                val t = 2f - xsi * xsi - ysj * ysj - zsk * zsk
                n +=
                    if(t < 0) 0f
                    else {
                        val tp = t * t
                        tp * g.dot(xsi, ysj, zsk)
                    }
            }

        }
    }

    return n * 0.07775896f
}

fun main(args: Array<String>) {
    val width = 1024
    val height = 1024
    val depth = 1024
    val subdivision = 32

    var max = -1f



    for(times in 0 until 10) {
        println("Time: $times")
        val p = perm()
        for (x in -width until width) {
            val start = System.currentTimeMillis()
            for (y in -height until height) {
                for (z in -depth until depth) {
                    val xs = x / subdivision.toFloat()
                    val ys = y / subdivision.toFloat()
                    val zs = z / subdivision.toFloat()
                    val n = abs(myperlin(defaultPerlin3Grads, p, xs, ys, zs))
                    if (n > max) max = n
                }
            }
            val duration = (System.currentTimeMillis() - start) / 1000f
            println("Progress: ${(x + width) / (2 * width).toDouble()}, Loop time taken: $duration")
        }
        println(1 / max)
    }

    println(1 / max)
}