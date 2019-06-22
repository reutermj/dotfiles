package rewrite

import kotlin.math.*

val r2: Float = sqrt(2f)

fun ffloor(f: Float): Int {
    if (f >= 0) return f.toInt()
    else return f.toInt() - 1
}

fun lerp(a: Float, b: Float, t: Float): Float {
    return ((1 - t) * a) + (t * b)
}

fun smoothstep(a: Float, b: Float, x: Float): Float {
    if(x < a) return 0f
    else if(x >= b) return 1f
    else {
        val xp = (x - a) / (b - a)
        return x * x * (3f - 2f * x)
    }
}

fun unskew(x: Float, y: Float): Pair<Float, Float> {
    val G2 = (3f - sqrt(3f))/6f
    val t = (x + y) * G2
    return Pair(x - t, y - t)
}

fun clamp(x: Float, mn: Float, mx: Float): Float = max(mn, min(x, mx))