package rewrite

fun perlinInterp(t: Float): Float {
    return t * t * t * (t * (t * 6 - 15) + 10)
}

fun perlin(grads: Gradients<Vector1>, perm: PermutationTable, x: Float): Float {
    val xf = ffloor(x)

    val xs = x - xf

    val g0 = grads[perm[xf]]
    val g1 = grads[perm[xf + 1]]

    val n0 = g0.dot(xs)
    val n1 = g1.dot(xs - 1)

    val u = perlinInterp(xs)

    return lerp(n0, n1, u) * 2.05214761557f
}

fun perlin(grads: Gradients<Vector2>, perm: PermutationTable, x: Float, y: Float): Float {
    val xf = ffloor(x)
    val yf = ffloor(y)

    val xs = x - xf
    val ys = y - yf

    val g00 = grads[perm[xf, yf]]
    val g01 = grads[perm[xf, yf + 1]]
    val g10 = grads[perm[xf + 1, yf]]
    val g11 = grads[perm[xf + 1, yf + 1]]

    val n00 = g00.dot(xs, ys)
    val n01 = g01.dot(xs, ys - 1)
    val n10 = g10.dot(xs - 1, ys)
    val n11 = g11.dot(xs - 1, ys - 1)

    val u = perlinInterp(xs)
    val v = perlinInterp(ys)

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

    val g000 = grads[perm[xf, yf, zf]]
    val g001 = grads[perm[xf, yf, zf + 1]]
    val g010 = grads[perm[xf, yf + 1, zf]]
    val g011 = grads[perm[xf, yf + 1, zf + 1]]
    val g100 = grads[perm[xf + 1, yf, zf]]
    val g101 = grads[perm[xf + 1, yf, zf + 1]]
    val g110 = grads[perm[xf + 1, yf + 1, zf]]
    val g111 = grads[perm[xf + 1, yf + 1, zf + 1]]

    val n000 = g000.dot(xs, ys, zs)
    val n001 = g001.dot(xs, ys, zs - 1)
    val n010 = g010.dot(xs, ys - 1, zs)
    val n011 = g011.dot(xs, ys - 1, zs - 1)
    val n100 = g100.dot(xs - 1, ys, zs)
    val n101 = g101.dot(xs - 1, ys, zs - 1)
    val n110 = g110.dot(xs - 1, ys - 1, zs)
    val n111 = g111.dot(xs - 1, ys - 1, zs - 1)

    val u = perlinInterp(xs)
    val v = perlinInterp(ys)
    val w = perlinInterp(zs)

    val nx00 = lerp(n000, n100, u)
    val nx01 = lerp(n001, n101, u)
    val nx10 = lerp(n010, n110, u)
    val nx11 = lerp(n011, n111, u)

    val nxy0 = lerp(nx00, nx10, v)
    val nxy1 = lerp(nx01, nx11, v)

    val nxyz = lerp(nxy0, nxy1, w)

    return nxyz
}