package rewrite

fun fractal1(octaves: Int, lacunarity: Float, f: (Gradients<Vector1>, PermutationTable, Float) -> Float): (Gradients<Vector1>, PermutationTable, Float) -> Float {
    val gain = 1 / lacunarity
    return { grads, perm, x ->
            var noise = 0f



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

            noise / max
        }
}

fun fractal2(octaves: Int, lacunarity: Float, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): (Gradients<Vector2>, PermutationTable, Float, Float) -> Float {
    val gain = 1 / lacunarity
    return { grads, perm, x, y ->
            var noise = 0f



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

            noise / max
        }
}

fun fractal3(octaves: Int, lacunarity: Float, f: (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Float): (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Float {
    val gain = 1 / lacunarity
    return { grads, perm, x, y, z ->
            var noise = 0f



            var amplitude = 1f
            var frequency = 1f

            var max = 0f

            for (i in 0 until octaves) {
                max += amplitude
                val n = f(grads, perm, frequency * x, frequency * y, frequency * z)
                noise += amplitude * n
                frequency *= lacunarity
                amplitude *= gain
            }

            noise / max
        }
}