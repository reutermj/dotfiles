package rewrite

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

fun main(args: Array<String>) {
    val width = 4096 * 4
    val subdivision = 128

    /*testAreaUnderCurve(width, subdivision, ::perlin)
    testAreaUnderCurve(width, subdivision, ::myperlin)
    testAreaUnderCurve(width, subdivision, distNoise(0.5f, ::perlin, vecNoise1(::perlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.5f, ::myperlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.5f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.5f, ::myperlin, vecNoise1(::perlin)))*/

    /*testAreaUnderCurve(width, subdivision, distNoise(0.1f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.2f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.3f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.4f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.5f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.6f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.7f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.8f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(0.9f, ::perlin, vecNoise1(::myperlin)))
    testAreaUnderCurve(width, subdivision, distNoise(1.0f, ::perlin, vecNoise1(::myperlin)))*/

    testAreaUnderCurve(width, subdivision, ::perlin)
    //testAreaUnderCurve(width, subdivision, distNoise(0.5f, ::perlin, vecNoise1(::perlin)))
}

fun testAreaUnderCurve(width: Int, subdivision: Int, f: (Gradients<Vector1>, PermutationTable, Float) -> Float) {
    val rectWidth = 1f / subdivision.toFloat()

    var positiveArea = 0f
    var negativeArea = 0f

    var positive1 = 0f
    var positive2 = 0f
    var positive3 = 0f
    var positive4 = 0f
    var positive5 = 0f

    var negative1 = 0f
    var negative2 = 0f
    var negative3 = 0f
    var negative4 = 0f
    var negative5 = 0f

    val times = 1000
    for(i in 0 until times) {

        val rand = Random(System.nanoTime())
        val ptest = perm(rand, 256)
        val testPerlinGradients = genPerlin1Grads(128, rand)

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

            if(y > 0f) positiveArea += y * rectWidth
            if(y < 0f) negativeArea += y * rectWidth

            if(xs < 0.1f) {
                if(y > 0f) positive1 += y * rectWidth
                if(y < 0f) negative1 += y * rectWidth
            } else if(xs < 0.2f) {
                if(y > 0f) positive2 += y * rectWidth
                if(y < 0f) negative2 += y * rectWidth
            } else if(xs < 0.3f) {
                if(y > 0f) positive3 += y * rectWidth
                if(y < 0f) negative3 += y * rectWidth
            } else if(xs < 0.4f) {
                if(y > 0f) positive4 += y * rectWidth
                if(y < 0f) negative4 += y * rectWidth
            } else if(xs < 0.5f) {
                if(y > 0f) positive5 += y * rectWidth
                if(y < 0f) negative5 += y * rectWidth
            }
        }


    }

    println("positiveArea: $positiveArea, negativeArea: $negativeArea, difference: ${positiveArea + negativeArea}")
    println("positive1: $positive1, negative1: $negative1, difference: ${positive1 + negative1}")
    println("positive2: $positive2, negative2: $negative2, difference: ${positive2 + negative2}")
    println("positive3: $positive3, negative3: $negative3, difference: ${positive3 + negative3}")
    println("positive4: $positive4, negative4: $negative4, difference: ${positive4 + negative4}")
    println("positive5: $positive5, negative5: $negative5, difference: ${positive5 + negative5}")
}