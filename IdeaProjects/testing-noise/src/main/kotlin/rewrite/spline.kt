package rewrite

val CROO = -0.5f
val CR01 = 1.5f
val CR02 = -1.5f
val CR03 = 0.5f
val CR10 = 1.0f
val CR11 = -2.5f
val CR12 = 2.0f
val CR13 = -0.5f
val CR20 = -0.5f
val CR21 = 0.0f
val CR22 = 0.5f
val CR23 = 0.0f
val CR30 = 0.0f
val CR31 = 1.0f
val CR32 = 0.0f
val CR33 = 0.0f

fun spline(x: Float, knot: FloatArray): Float {
    val nspans = knot.size - 4
    if (nspans < 1) { //error
        return 0f
    }

    var xs = clamp(x, 0f, 1f) * nspans
    var span = xs.toInt()
    if(span >= nspans) span = nspans

    xs -= span

    //println("x: $x, span: $span")

    val c3 = CROO*knot[span] + CR01*knot[span + 1] + CR02*knot[span + 2] + CR03*knot[span + 3]
    val c2 = CR10*knot[span] + CR11*knot[span + 1] + CR12*knot[span + 2] + CR13*knot[span + 3]
    val c1 = CR20*knot[span] + CR21*knot[span + 1] + CR22*knot[span + 2] + CR23*knot[span + 3]
    val c0 = CR30*knot[span] + CR31*knot[span + 1] + CR32*knot[span + 2] + CR33*knot[span + 3]



    //println("c0: $c0, c1: $c1, c2: $c2, c3: $c3")

    return ((c3 * xs + c2) * xs + c1) * xs + c0
}