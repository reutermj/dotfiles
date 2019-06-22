package rewrite

import kotlin.math.sqrt

fun mysimplex(grads: Gradients<Vector2>, perm: PermutationTable, xi: Float, yi: Float): Float {
    val x = xi * 1.46875f
    val y = yi * 1.46875f
    val F2 = 0.5f * (sqrt(3f) - 1f)
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val G2 = (3f - sqrt(3f)) / 6f
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

    if (x0 < y0) {
        gx10 = gx3
        gy10 = gy2
        x10 = x3 + G2
        y10 = y2 - G2

        gx11 = gx2
        gy11 = gy8
        x11 = x2 + G2
        y11 = y8 - G2
    }

    val g0 = grads[perm[gx0, gy0]]
    val g1 = grads[perm[gx1, gy1]]
    val g2 = grads[perm[gx2, gy2]]
    val g3 = grads[perm[gx3, gy3]]
    val g4 = grads[perm[gx4, gy4]]
    val g5 = grads[perm[gx5, gy5]]
    val g6 = grads[perm[gx6, gy6]]
    val g7 = grads[perm[gx7, gy7]]
    val g8 = grads[perm[gx8, gy8]]
    val g9 = grads[perm[gx9, gy9]]
    val g10 = grads[perm[gx10, gy10]]
    val g11 = grads[perm[gx11, gy11]]

    val n0 = radialAttenuation(1.2f, g0, x0, y0)
    val n1 = radialAttenuation(1.2f, g1, x1, y1)
    val n2 = radialAttenuation(1.2f, g2, x2, y2)
    val n3 = radialAttenuation(1.2f, g3, x3, y3)
    val n4 = radialAttenuation(1.2f, g4, x4, y4)
    val n5 = radialAttenuation(1.2f, g5, x5, y5)
    val n6 = radialAttenuation(1.2f, g6, x6, y6)
    val n7 = radialAttenuation(1.2f, g7, x7, y7)
    val n8 = radialAttenuation(1.2f, g8, x8, y8)
    val n9 = radialAttenuation(1.2f, g9, x9, y9)
    val n10 = radialAttenuation(1.2f, g10, x10, y10)
    val n11 = radialAttenuation(1.2f, g11, x11, y11)

    return (n0 + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + n10 + n11) * 0.77358899108f
}