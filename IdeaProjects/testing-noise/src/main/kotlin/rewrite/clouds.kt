package rewrite

import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random

fun puffyClouds(skycolor: Vector3, cloudColor: Vector3,
                threshold: Float,
                width: Int, height: Int, subdivisions: Int,
                noise: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): BufferedImage {

    val rand = Random(System.nanoTime())
    val grads = genPerlin2Grads(64, rand)
    val p = perm(rand, 256)

    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for(x in 0 until width) {
        for(y in 0 until height) {
            val n = (noise(grads, p, x / subdivisions.toFloat() + 15f, y / subdivisions.toFloat()) + 0.3f)/1.25f
            val r = (lerp(skycolor.x, cloudColor.x, smoothstep(threshold, 1f, n)) * 256).toInt()
            val g = (lerp(skycolor.y, cloudColor.y, smoothstep(threshold, 1f, n)) * 256).toInt()
            val b = (lerp(skycolor.z, cloudColor.z, smoothstep(threshold, 1f, n)) * 256).toInt()
            val rgb = (r shl 16) or (g shl 8) or b
            img.setRGB(x, height - y - 1, rgb)
        }
    }

    return img
}

fun cloudsDrawPerlinLines(img: BufferedImage, width: Int, height: Int, subdivisions: Int) {
    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.color = Color.MAGENTA

    for(x in 0 until width step subdivisions) {
        g2d.drawLine(x, 0, x, height)
    }

    for(y in 0 until height step subdivisions) {
        g2d.drawLine(0, y, width, y )
    }
}

fun cloudsDrawSimplexLines(img: BufferedImage, width: Int, height: Int, subdivisions: Int) {
    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.color = Color.MAGENTA

    for(x in 0 until 20) {
        for (y in 0 until 20) {
            val (x0, y0) = unskew(x.toFloat(), y.toFloat())
            val (x1, y1) = unskew(x.toFloat() + 1, y.toFloat())
            val (x2, y2) = unskew(x.toFloat() + 1, y.toFloat() + 1)
            g2d.drawLine((x0 * subdivisions).toInt(), height - (y0 * subdivisions).toInt(), (x1 * subdivisions).toInt(), height - (y1 * subdivisions).toInt())
            g2d.drawLine((x0 * subdivisions).toInt(), height - (y0 * subdivisions).toInt(), (x2 * subdivisions).toInt(), height - (y2 * subdivisions).toInt())
            g2d.drawLine((x1 * subdivisions).toInt(), height - (y1 * subdivisions).toInt(), (x2 * subdivisions).toInt(), height - (y2 * subdivisions).toInt())
        }
    }
}


fun main(args: Array<String>) {
    try {
        val width = 1024
        val height = 1024
        val subdivisions = 128

        val img = puffyClouds(Vector3(0.15f, 0.15f, 0.6f), Vector3(1f, 1f, 1f), 0f,
            width, height, subdivisions,
            fractal2(8, 2f, distNoise(0.5f, ::simplex, vecNoise2(::simplex))))
        cloudsDrawSimplexLines(img, width, height, subdivisions)
        val f = File("/home/mark/noisestuff/clouds.png")
        ImageIO.write(img, "png", f)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}