package rewrite

import java.io.File
import kotlin.math.*
import kotlin.random.Random

fun main(args: Array<String>) {
    noiseDistribution1D()
}

fun noiseDistribution1D() {
    try {
        val width = 4096
        val subdivision = 128
        val noiseFunctions = arrayOf<(Gradients<Vector1>, PermutationTable, Float) -> Float>(
            distNoise(0.75f, 0.5f, ::perlin, vecNoise1(::perlin)),
            distNoise(0.5f, 0.25f, ::perlin, vecNoise1(::perlin)),
            distNoise(0.75f, 0.5f, ::perlin, vecNoise1(::perlin)),
            distNoise(0.5f, 0.25f, ::perlin, vecNoise1(::perlin)),
            distNoise(0.75f, 0.5f, ::perlin, vecNoise1(::perlin)),
            distNoise(0.5f, 0.25f, ::perlin, vecNoise1(::perlin))
        )
        val csvs = noiseFunctions.map { test1DNoiseDistribution(width, subdivision, it)}
        for(i in 0 until csvs.size) {
            val f = File("/home/mark/noisestuff/1d-stats$i.csv")
            f.writeText(csvs[i])
        }
    } catch (e: Exception) {

    }
}

fun test1DNoiseDistribution(width: Int, subdivision: Int, f: (Gradients<Vector1>, PermutationTable, Float) -> Float): String {
    val buckets1 = Buckets()
    val buckets2 = Buckets()
    val buckets3 = Buckets()
    val buckets4 = Buckets()
    val buckets5 = Buckets()
    val bucketsat = Buckets()

    val times = 10000
    for(i in 0 until times) {

        val rand = Random(System.nanoTime())
        val ptest = perm(rand, 256)
        val testPerlinGradients = genBalancedPerlin1Grads(128, rand)

        var normalize = -1f

        for (x in -width until width) {
            val xf = x / subdivision.toFloat()
            val y = abs(f(testPerlinGradients, ptest, xf))
            if (y > normalize) normalize = y
        }

        normalize = 1 / normalize


        for (x in -width until width) {
            val xf = x / subdivision.toFloat()
            val xr = xf.roundToInt()
            val xs = abs(xf - xr)
            val y = normalize * f(testPerlinGradients, ptest, xf)


            if(xs <= 0.1f) buckets1.insert(y)
            else if(xs <= 0.2f) buckets2.insert(y)
            else if(xs <= 0.3f) buckets3.insert(y)
            else if(xs <= 0.4f) buckets4.insert(y)
            else if(xs <= 0.5f) buckets5.insert(y)

            if (x.rem(subdivision) == 0) {
                bucketsat.insert(y)
            }
        }
    }

    val buffer = StringBuffer()

    for(i in 0 until 20)
        buffer.append("${bucketsat.buckets[i] / times.toFloat()},${buckets1.buckets[i] / times.toFloat()},${buckets2.buckets[i] / times.toFloat()},${buckets3.buckets[i] / times.toFloat()},${buckets4.buckets[i] / times.toFloat()},${buckets5.buckets[i] / times.toFloat()}\n")
    return buffer.toString()
}