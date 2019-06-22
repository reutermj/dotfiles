package rewrite

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

sealed class Vector(val size: Int)
data class Vector1(val x: Float): Vector(1) {
    fun dot(x1: Float): Float {
        return x * x1
    }
}
data class Vector2(val x: Float, val y: Float): Vector(2) {
    fun dot(x1: Float, y1: Float): Float {
        return x * x1 + y * y1
    }
}

data class Vector3(val x: Float, val y: Float, val z: Float): Vector(3) {
    fun dot(x1: Float, y1: Float, z1: Float): Float {
        return x * x1 + y * y1 + z * z1
    }
}

data class Gradients<V: Vector>(val grads: Array<V>) {
    override fun toString(): String {
        return "<$grads[0], $grads[1]>"
    }

    operator fun get(index: Int): V {
        return grads[index.rem(grads.size)]
    }
}

fun genBalancedPerlin1Grads(i: Int, rand: Random): Gradients<Vector1> {
    val arr = Array(i / 2) {
        rand.nextFloat()
    }

    val arr2 = Array(i) {
        Vector1(arr[it / 2] * if(it % 2 == 0) -1 else -1)
    }

    return Gradients(arr2)
}

fun genPerlin1Grads(i: Int, rand: Random): Gradients<Vector1> {
    val arr = Array(i) {
        Vector1(rand.nextFloat() * 2f - 1f)
    }
    return Gradients(arr)
}

fun genPerlin2Grads(i: Int, rand: Random): Gradients<Vector2> {
    val arr = Array(i) {
        val rad = rand.nextFloat() * 2 * PI
        Vector2(cos(rad).toFloat(), sin(rad).toFloat())
    }
    return Gradients(arr)
}

val defaultPerlin3Grads: Gradients<Vector3> =
        Gradients(
            arrayOf(
                Vector3(1f, 1f, 0f), Vector3(-1f, 1f, 0f), Vector3(1f, -1f, 0f), Vector3(-1f, -1f, 0f),
                Vector3(1f, 0f, 1f), Vector3(-1f, 0f, 1f), Vector3(1f, 0f, -1f), Vector3(-1f, 0f, -1f),
                Vector3(0f, 1f, 1f), Vector3(0f, -1f, 1f), Vector3(0f, 1f, -1f), Vector3(0f, -1f, -1f))
        )