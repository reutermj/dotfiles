package rewrite

import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.random.Random

val rKnots: FloatArray =
        floatArrayOf(
            0f, 0f, 27f, 54f,
            81f, 109f, 136f, 166f,
            189f, 211f, 231f, 238f,
            244f, 248f, 251f, 254f,
            255f, 255f)

val gKnots: FloatArray =
        floatArrayOf(
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 5f,
            30f, 60f, 91f, 128f,
            162f, 187f, 209f, 236f,
            241f, 241f)

val bKnots: FloatArray =
        floatArrayOf(
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            12f, 58f, 115f, 210f,
            230f, 230f)

fun fire(grads: Gradients<Vector2>, perm: PermutationTable,
         width: Int, height: Int, subdivision: Int,
         flameAmplitude: Float, flameHeight: Float,
         chaosOffset: Float, chaosScale: Float,
         f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for(x in 0 until width) {
        for(y in 0 until height) {
            val xs = x / subdivision.toFloat()
            val ys = y / height.toFloat()
            val ye = exp(-(y / height.toFloat()))

            val chaos = abs(f(grads, perm, xs, ye) * chaosScale + chaosOffset)
            val cmap = 0.85f * chaos + 0.8f * (flameAmplitude - flameHeight * ys)
            val r = (spline(cmap, rKnots).roundToInt() and 0xff) shl 16
            val g = (spline(cmap, gKnots).roundToInt() and 0xff) shl 8
            val b = spline(cmap, bKnots).roundToInt() and 0xff
            val rgb = r or g or b
            img.setRGB(x, height - y - 1, rgb)
        }
    }


    fireDrawPerlinLines(img, width, height, subdivision)
    return img
}

fun fireDrawPerlinLines(img: BufferedImage, width: Int, height: Int, subdivision: Int) {
    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.color = Color.MAGENTA

    for(x in 0 until width step subdivision) {
        g2d.drawLine(x, 0, x, height)
    }
}

fun main(args: Array<String>) {
    try {
        val rand = Random(System.nanoTime())
        val grads = genPerlin2Grads(128, rand)
        val perm = perm(rand, 256)
        val width = 1024
        val height = 1024
        val subdivision = 128

        /*for(y in 0 until height) {
            println(exp(-(y / height.toFloat())))
        }*/

        val noiseFunctions = arrayOf(
            fractal2(7, 2f, ::perlin),
            fractal2(7, 2f, distNoise(0.5f, ::perlin, vecNoise2(::perlin))),
            //fractal2(7, 2f, ::simplex),
            //fractal2(7, 2f, distNoise(0.5f, ::simplex, vecNoise2(::simplex))),
            fractal2(7, 2f, ::myperlin),
            fractal2(7, 2f, ::mysimplex)
        )
        /*val noiseFunctions = arrayOf(
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 0f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 1f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 2f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 3f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 4f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 5f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 6f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 7f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 8f)}), vecNoise2(::perlin))
        )*/

        /*val noiseFunctions = arrayOf(
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 0f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 1f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 2f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 3f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 4f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 5f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 6f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 7f)}), vecNoise2(::perlin)),
            distNoise(0.5f, fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 8f)}), vecNoise2(::perlin))
        )*/

        /*val noiseFunctions = arrayOf(
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 0f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 1f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 2f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 3f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 4f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 5f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 6f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 7f)}),
            fractal2(7, 2f, {_, p, x, y -> perlin(defaultPerlin3Grads, p, x, y, 8f)})
        )*/

        /*val noiseFunctions = arrayOf(
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 0f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 1f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 2f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 3f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 4f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 5f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 6f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 7f)}),
            fractal2(7, 2f, {_, p, x, y -> myperlin(defaultPerlin3Grads, p, x, y, 8f)})
        )*/

        val images =
            noiseFunctions.map {fire(
                grads, perm,
                width, height, subdivision,
                0.75f, 1.1f,
                0f, 1f,
                it
            )}

        /*val permutations = Array(9) {perm(rand, 256)}

        val images =
            permutations.map {
                fire(
                    grads, it,
                    width, height, subdivision,
                    0.8f, 1f,
                    0f, 1f, ::simplex
                )
            }*/

        for(i in 0 until images.size) {
            val file = File("/home/mark/noisestuff/fire-$i.png")
            ImageIO.write(images[i], "png", file)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}