package rewrite

import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random

fun main(args: Array<String>) {
    draw1dNoiseTest()
}

fun draw1dNoiseTest() {
    try {
        val perm = perm()
        val grads = genPerlin1Grads(256, Random(System.nanoTime()))
        val width = 1024
        val height = 1024
        val subdivisions = 64f

        /*val noiseFunctions = arrayOf<(Gradients<Vector1>, PermutationTable, Float) -> Float>(
            ::perlin)
        val images = noiseFunctions.map {gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.75f, 0.5f, it)}
        for (i in 0 until noiseFunctions.size) {
            val f = File("/home/mark/noisestuff/1d-img$i.png")
            ImageIO.write(images[i], "png", f)
        }*/

        val img1 = gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.75f, 0.5f, ::perlin)
        val img2 = gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.75f, 0.25f, ::perlin)
        val img3 = gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.5f, 0.5f, ::perlin)
        val img4 = gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.5f, 0.25f, ::perlin)
        val img5 = gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.25f, 0.5f, ::perlin)
        val img6 = gen1DNoiseDistortionOverlay(grads, perm, width, height, subdivisions, false, 0.25f, 0.5f, ::perlin)
        val f1 = File("/home/mark/noisestuff/1d-img1.png")
        val f2 = File("/home/mark/noisestuff/1d-img2.png")
        val f3 = File("/home/mark/noisestuff/1d-img3.png")
        val f4 = File("/home/mark/noisestuff/1d-img4.png")
        val f5 = File("/home/mark/noisestuff/1d-img5.png")
        val f6 = File("/home/mark/noisestuff/1d-img6.png")
        ImageIO.write(img1, "png", f1)
        ImageIO.write(img1, "png", f2)
        ImageIO.write(img1, "png", f3)
        ImageIO.write(img1, "png", f4)
        ImageIO.write(img1, "png", f5)
        ImageIO.write(img1, "png", f6)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun draw2dNoiseTest() {
    try {
        val perm = perm()
        val grads = genPerlin2Grads(256, Random(System.nanoTime()))
        val width = 1024
        val height = 1024
        val subdivisions = 64f

        val noiseFunctions = arrayOf<(Gradients<Vector2>, PermutationTable, Float, Float) -> Float>(
            ::perlin,
            ::myperlin,
            ::simplex,
            ::mysimplex,
            distNoise(0.5f, ::perlin, vecNoise2(::perlin)),
            distNoise(0.5f, ::myperlin, vecNoise2(::myperlin)),
            distNoise(0.5f, ::simplex, vecNoise2(::simplex)),
            distNoise(0.5f, ::mysimplex, vecNoise2(::mysimplex)))
        val images = noiseFunctions.map {gen2DImage(grads, perm, width, height, subdivisions, true, ::noiseToGrayscaleWithZeroes, it)}
        for (i in 0 until noiseFunctions.size) {
            val f = File("/home/mark/noisestuff/2d-img$i.png")
            ImageIO.write(images[i], "png", f)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}