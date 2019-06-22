package rewrite

import kotlin.math.sqrt

val F2 = 0.5f * (sqrt(3f) - 1f)
val G2 = (3f - sqrt(3f))/6f
val F3 = 1f/3f
val G3 = 1f/6f

fun radialAttenuation(dst: Float, g: Vector1, x: Float): Float {
    val t = dst - x * x
    if(t < 0f) return 0f
    else {
        val tp = t * t
        return tp * tp * g.dot(x)
    }
}

fun radialAttenuation(dst: Float, g: Vector2, x: Float, y: Float): Float {
    val t = dst - x * x - y * y
    if(t < 0f) return 0f
    else {
        val tp = t * t
        return tp * tp * g.dot(x, y)
    }
}

fun radialAttenuation(dst: Float, g: Vector3, x: Float, y: Float, z: Float): Float {
    val t = dst - x * x - y * y - z * z
    if(t < 0f) return 0f
    else {
        val tp = t * t
        return tp * tp * g.dot(x, y, z)
    }
}

fun simplex(grads: Gradients<Vector2>, perm: PermutationTable, xs: Float, ys: Float): Float {
    val x = xs
    val y = ys
    val s = (x + y) * F2
    val i = ffloor(x + s)
    val j = ffloor(y + s)

    val t = (i + j) * G2

    val X0 = i - t
    val Y0 = j - t
    val x0 = x - X0
    val y0 = y - Y0

    val i1: Int
    val j1: Int
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

    val g0 = grads[perm[i, j]]
    val g1 = grads[perm[i + i1, j + j1]]
    val g2 = grads[perm[i + 1, j + 1]]

    val n0 = radialAttenuation(0.5f, g0, x0, y0)
    val n1 = radialAttenuation(0.5f, g1, x1, y1)
    val n2 = radialAttenuation(0.5f, g2, x2, y2)

    return (n0 + n1 + n2) * 70f
}

fun simplex(grads: Gradients<Vector3>, perm: PermutationTable, x: Float, y: Float, z: Float): Float {
    val s = (x + y + z) * F3
    val i = ffloor(x + s)
    val j = ffloor(y + s)
    val k = ffloor(z + s)

    val t = (i + j + k) * G3

    val X0 = i - t
    val Y0 = j - t
    val Z0 = k - t

    val x0 = x - X0
    val y0 = y - Y0
    val z0 = z - Z0

    val i1: Int
    val j1: Int
    val k1: Int

    val i2: Int
    val j2: Int
    val k2: Int

    if(x0 >= y0) {
        if(y0 >= z0) {
            i1 = 1
            j1 = 0
            k1 = 0
            i2 = 1
            j2 = 1
            k2 = 0
        } else if(x0 >= z0) {
            i1 = 1
            j1 = 0
            k1 = 0
            i2 = 1
            j2 = 0
            k2 = 1
        } else {
            i1 = 0
            j1 = 0
            k1 = 1
            i2 = 1
            j2 = 0
            k2 = 1
        }
    } else {
        if(y0 < z0) {
            i1 = 0
            j1 = 0
            k1 = 1
            i2 = 0
            j2 = 1
            k2 = 1
        } else if(x0 < z0) {
            i1 = 0
            j1 = 1
            k1 = 0
            i2 = 0
            j2 = 1
            k2 = 1
        } else {
            i1 = 0
            j1 = 1
            k1 = 0
            i2 = 1
            j2 = 1
            k2 = 0
        }
    }

    val x1 = x0 - i1 + G3
    val y1 = y0 - j1 + G3
    val z1 = z0 - k1 + G3

    val x2 = x0 - i2 + 2.0f * G3
    val y2 = y0 - j2 + 2.0f * G3
    val z2 = z0 - k2 + 2.0f * G3

    val x3 = x0 - 1.0f + 3.0f * G3
    val y3 = y0 - 1.0f + 3.0f * G3
    val z3 = z0 - 1.0f + 3.0f * G3

    val g0 = grads[perm[i, j, k]]
    val g1 = grads[perm[i + i1, j + j1, k + k1]]
    val g2 = grads[perm[i + i2, j + j2, k + k2]]
    val g3 = grads[perm[i + 1, j + 1, k + 1]]

    val n0 = radialAttenuation(0.5f, g0, x0, y0, z0)
    val n1 = radialAttenuation(0.5f, g1, x1, y1, z1)
    val n2 = radialAttenuation(0.5f, g2, x2, y2, z2)
    val n3 = radialAttenuation(0.5f, g3, x3, y3, z3)

    return (n0 + n1 + n2 + n3) * 32f
}