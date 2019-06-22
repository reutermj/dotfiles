package noise

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

sealed class Vector
data class Vector1(val x: Float): Vector()
data class Vector2(val x: Float, val y: Float): Vector()
data class Vector3(val x: Float, val y: Float, val z: Float): Vector()


data class Gradients<V: Vector>(val grads: Array<V>) {
    override fun toString(): String {
        return "<$grads[0], $grads[1]>"
    }
}
data class PermutationTable(val perm: Array<Int>)

fun perm(seed: Long): PermutationTable {
    val rand = Random(seed)
    val perm = Array<Int>(256) {it}
    for (c in 0 until 5) {
        for (i in 0 until 256) {
            val r = rand.nextInt(0, 256)
            val t = perm[i]
            perm[i] = perm[r]
            perm[r] = t
        }
    }
    return PermutationTable(perm)
}

fun g(grads: Gradients<Vector1>, perm: PermutationTable, x: Int): Vector1 {
    return grads.grads[perm.perm[x and 0xff].rem(grads.grads.size)]
}

fun g(grads: Gradients<Vector2>, perm: PermutationTable, x: Int, y: Int): Vector2 {
    return grads.grads[(perm.perm[x and 0xff] xor perm.perm[y and 0xff]).rem(grads.grads.size)]
}

fun g(grads: Gradients<Vector3>, perm: PermutationTable, x: Int, y: Int, z: Int): Vector3 {
    return grads.grads[(perm.perm[x and 0xff] xor perm.perm[y and 0xff] xor perm.perm[z and 0xff]).rem(grads.grads.size)]
}

val defaultPerlinGradients2: Gradients<Vector2> =
        Gradients(
            arrayOf(
                Vector2(1f, 0f),
                Vector2(sqrt(3f) / 2f, 0.5f),
                Vector2(sqrt(2f) / 2f, sqrt(2f) / 2f),
                Vector2(0.5f, sqrt(3f) / 2f),
                Vector2(0f, 1f),
                Vector2(-0.5f, sqrt(3f) / 2f),
                Vector2(-sqrt(2f) / 2f, sqrt(2f) / 2f),
                Vector2(-sqrt(3f) / 2f, 0.5f),
                Vector2(-1f, 0f),
                Vector2(-sqrt(3f) / 2f, -0.5f),
                Vector2(-sqrt(2f) / 2f, -sqrt(2f) / 2f),
                Vector2(-0.5f, -sqrt(3f) / 2f),
                Vector2(0f, -1f),
                Vector2(0.5f, -sqrt(3f) / 2f),
                Vector2(sqrt(2f) / 2f, -sqrt(2f) / 2f),
                Vector2(sqrt(3f) / 2f, -0.5f)
            )
        )

val defaultPerlinGradients3: Gradients<Vector3> =
        Gradients(
            arrayOf(
                Vector3(1f, 1f, 0f),
                Vector3(-1f, 1f, 0f),
                Vector3(1f, -1f, 0f),
                Vector3(-1f, -1f, 0f),
                Vector3(1f, 0f, 1f),
                Vector3(-1f, 0f, 1f),
                Vector3(1f, 0f, -1f),
                Vector3(-1f, 0f, -1f),
                Vector3(0f, 1f, 1f),
                Vector3(0f, -1f, 1f),
                Vector3(0f, 1f, -1f),
                Vector3(0f, -1f, -1f)
            )
        )

val perlinOneGradients2: Gradients<Vector2> =
        Gradients(
            arrayOf(
                Vector2(1f, 0f),
                Vector2(-1f, 0f),
                Vector2(-0f, -1f),
                Vector2(0f, -1f)
            )
        )

fun genPerlinGrads(i: Int, rand: Random): Gradients<Vector2> {
    val arr = Array(i) {
        val rad = rand.nextFloat() * 2 * PI
        Vector2(cos(rad).toFloat(), sin(rad).toFloat())
    }
    return Gradients(arr)
}

fun genGrads(i: Int, rand: Random): Vector2 {
    val pi =
        if(i.rem(2) == 0) PI.toFloat()
        else PI.toFloat() / 2f

    val fuzz = (rand.nextFloat() * 2f - 1f) * 0.261799f
    val pif = pi + fuzz
    return Vector2(cos(pif), sin(pif))
}

val rand: Random = Random(System.nanoTime())

val polarizedGradients2: Gradients<Vector2> =
        Gradients(
            Array<Vector2>(32) { genGrads(it, rand)}
        )

fun genGrads2(i: Int, rand: Random): Vector2 {
    val pi =
        if(i.rem(4) == 0) 0f
        else if(i.rem(4) == 1) PI.toFloat() / 2f
        else if(i.rem(4) == 2) PI.toFloat()
        else (3 * PI.toFloat()) / 2f

    val fuzz = (rand.nextFloat() * 2f - 1f) * 0.261799f
    val pif = pi + fuzz
    return Vector2(cos(pif), sin(pif))
}

val allPolarizedGradients2: Gradients<Vector2> =
    Gradients(
        Array<Vector2>(32) { genGrads2(it, rand)}
    )

val defaultPerlinGradients1: Gradients<Vector1> =
        Gradients(
            Array<Vector1>(128) { Vector1(rand.nextFloat() * 2f - 1f) }
        )

val defaultPerlinAmplitudes1: FloatArray =
        FloatArray(128) {
            rand.nextFloat()
        }

val testPermTable: PermutationTable =
        PermutationTable(
            arrayOf(67, 190, 73, 155, 196, 30, 94, 22, 236, 95, 42, 111, 187, 38, 203, 181, 143, 93, 152, 75, 158, 194, 79, 116, 132, 6, 131, 218, 13, 137, 66, 102, 173, 171, 36, 153, 162, 87, 250, 14, 217, 109, 233, 154, 82, 19, 76, 170, 213, 89, 72, 176, 83, 25, 88, 241, 144, 240, 135, 228, 239, 77, 85, 62, 31, 205, 140, 4, 237, 54, 161, 114, 106, 227, 60, 200, 32, 231, 41, 189, 15, 91, 232, 178, 48, 206, 71, 101, 120, 57, 151, 43, 126, 37, 175, 199, 28, 192, 136, 220, 55, 198, 121, 20, 210, 10, 39, 124, 97, 122, 247, 64, 90, 133, 50, 238, 56, 105, 216, 29, 130, 229, 103, 191, 65, 167, 188, 108, 234, 226, 221, 27, 104, 141, 165, 53, 150, 1, 253, 127, 45, 201, 16, 139, 69, 80, 223, 142, 92, 115, 138, 185, 18, 68, 246, 212, 166, 225, 252, 243, 9, 180, 59, 235, 51, 207, 214, 193, 254, 40, 174, 17, 21, 222, 251, 184, 160, 110, 35, 33, 58, 208, 70, 112, 61, 86, 249, 118, 182, 145, 146, 5, 24, 186, 2, 156, 211, 98, 125, 168, 123, 99, 3, 34, 215, 44, 248, 128, 7, 244, 49, 134, 84, 255, 197, 159, 0, 245, 209, 119, 230, 169, 113, 11, 129, 100, 242, 224, 46, 81, 204, 163, 183, 195, 179, 177, 47, 172, 107, 78, 202, 26, 157, 74, 149, 164, 63, 219, 12, 117, 52, 147, 148, 23, 8, 96)
        )

val uniformRandomGradients1: Gradients<Vector1> =
        Gradients(
            arrayOf(-0.9140625, -0.953125, -0.9453125, -0.921875, -0.8828125, -0.8984375, -0.9296875, -0.9453125, -0.9453125, -0.90625, -0.875, -0.890625, -0.9375, -0.9296875, -0.9453125, -0.984375, -0.7890625, -0.8203125, -0.7890625, -0.7578125, -0.875, -0.75, -0.796875, -0.7578125, -0.7734375, -0.8515625, -0.7890625, -0.8046875, -0.765625, -0.859375, -0.828125, -0.8671875, -0.640625, -0.7109375, -0.7265625, -0.6953125, -0.75, -0.7109375, -0.7265625, -0.6484375, -0.671875, -0.6328125, -0.640625, -0.625, -0.75, -0.6875, -0.671875, -0.6796875, -0.5625, -0.578125, -0.546875, -0.5703125, -0.546875, -0.5859375, -0.5, -0.59375, -0.578125, -0.5546875, -0.5390625, -0.5, -0.578125, -0.5234375, -0.6171875, -0.625, -0.46875, -0.4296875, -0.40625, -0.4453125, -0.484375, -0.4296875, -0.453125, -0.375, -0.5, -0.4296875, -0.4140625, -0.46875, -0.3828125, -0.4453125, -0.421875, -0.390625, -0.2734375, -0.3359375, -0.359375, -0.28125, -0.3046875, -0.265625, -0.3515625, -0.2578125, -0.328125, -0.375, -0.265625, -0.2734375, -0.3203125, -0.375, -0.34375, -0.2734375, -0.1875, -0.1796875, -0.1796875, -0.1640625, -0.140625, -0.1640625, -0.171875, -0.1328125, -0.1796875, -0.2109375, -0.1640625, -0.1640625, -0.1796875, -0.125, -0.2421875, -0.15625, -0.1015625, -0.1171875, -0.0703125, -0.0703125, 0.0, -0.0390625, -0.0390625, -0.0546875, -0.09375, -0.0859375, -0.046875, -0.0078125, -0.03125, -0.0703125, -0.1171875, -0.109375, 0.0859375, 0.046875, 0.046875, 0.125, 0.0078125, 0.0859375, 0.0234375, 0.0078125, 0.0859375, 0.0859375, 0.1015625, 0.0078125, 0.03125, 0.0703125, 0.109375, 0.0546875, 0.2265625, 0.234375, 0.1875, 0.2109375, 0.1640625, 0.125, 0.1640625, 0.1640625, 0.1875, 0.21875, 0.1796875, 0.171875, 0.21875, 0.21875, 0.1875, 0.1953125, 0.2734375, 0.3046875, 0.25, 0.2890625, 0.2890625, 0.375, 0.25, 0.296875, 0.3203125, 0.296875, 0.359375, 0.2890625, 0.3203125, 0.2890625, 0.3203125, 0.28125, 0.453125, 0.3828125, 0.4453125, 0.484375, 0.40625, 0.4375, 0.4296875, 0.453125, 0.40625, 0.484375, 0.390625, 0.4453125, 0.3984375, 0.421875, 0.3984375, 0.46875, 0.53125, 0.5859375, 0.578125, 0.5546875, 0.515625, 0.5078125, 0.5390625, 0.5390625, 0.5390625, 0.5546875, 0.5859375, 0.625, 0.5390625, 0.515625, 0.5, 0.6015625, 0.640625, 0.671875, 0.6875, 0.6484375, 0.6640625, 0.6640625, 0.734375, 0.6875, 0.6796875, 0.65625, 0.6796875, 0.65625, 0.640625, 0.640625, 0.6875, 0.7109375, 0.8671875, 0.8671875, 0.8671875, 0.828125, 0.8359375, 0.8046875, 0.8046875, 0.8359375, 0.8203125, 0.8359375, 0.8046875, 0.765625, 0.7890625, 0.7578125, 0.8046875, 0.78125, 0.9140625, 0.875, 0.8828125, 0.9453125, 0.890625, 0.9296875, 0.8828125, 0.9140625, 0.9609375, 0.9140625, 0.8984375, 0.9453125, 0.9296875, 0.96875, 0.8984375, 1.0)
                .map { Vector1(it.toFloat()) }.toTypedArray()
        )