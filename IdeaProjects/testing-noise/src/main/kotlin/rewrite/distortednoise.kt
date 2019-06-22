package rewrite

fun distNoise(distortion: Float, offset: Float,
              f: (Gradients<Vector1>, PermutationTable, Float) -> Float,
              vecNoise: (Gradients<Vector1>, PermutationTable, Float) -> Vector1):
            (Gradients<Vector1>, PermutationTable, Float) -> Float =
    {
        grads, perm, x ->
        val offset = vecNoise(grads, perm, x + offset)
        f(grads, perm, x + (offset.x * distortion))
    }

fun distNoise(distortion: Float,
              f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float,
              vecNoise: (Gradients<Vector2>, PermutationTable, Float, Float) -> Vector2):
            (Gradients<Vector2>, PermutationTable, Float, Float) -> Float =
    {
        grads, perm, x, y ->
        val offset = vecNoise(grads, perm, x + 0.5f, y + 0.5f)
        f(grads, perm, x + (offset.x * distortion), y + (offset.y * distortion))
    }

fun distNoise(distortion: Float,
              f: (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Float,
              vecNoise: (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Vector3):
            (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Float =
    {
        grads, perm, x, y, z ->
        val offset = vecNoise(grads, perm, x + 0.5f, y + 0.5f, z + 0.5f)
        f(grads, perm, x + (offset.x * distortion), y + (offset.y * distortion), z + (offset.z * distortion))
    }

fun vecNoise1(f: (Gradients<Vector1>, PermutationTable, Float) -> Float): (Gradients<Vector1>, PermutationTable, Float) -> Vector1 =
    {grads, perm, x -> Vector1(f(grads, perm, x))}

fun vecNoise2(f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): (Gradients<Vector2>, PermutationTable, Float, Float) -> Vector2 =
    {grads, perm, x, y -> Vector2(f(grads, perm, x, y), f(grads, perm, x + 3.33f, y + 3.33f))}

fun vecNoise3(f: (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Float): (Gradients<Vector3>, PermutationTable, Float, Float, Float) -> Vector3 =
    {grads, perm, x, y, z -> Vector3(f(grads, perm, x, y, z), f(grads, perm, x + 3.33f, y + 3.33f, z + 3.33f), f(grads, perm, x + 7.77f, y + 7.77f, z + 7.77f))}

