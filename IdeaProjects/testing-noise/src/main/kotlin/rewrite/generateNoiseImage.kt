package rewrite

import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs


fun noiseToGrayscale(f: Float): Int {
    val f1 = (f + 1) / 2
    val i = (f1 * 256).toInt() and 0xff
    return i * 0x010101
}

fun noiseToGrayscaleWithZeroes(f: Float): Int {
    if((-0.02f <= f) and (f <= 0.02f))
        return 0xff0000
    val f1 = (f + 1) / 2
    val i = (f1 * 256).toInt() and 0xff
    return i * 0x010101
}

fun fuzzEq(a: Float, b: Float, fuzz: Float): Boolean {
    return (a >= b - fuzz) and (a <= b + fuzz)
}

fun nearInt(f: Float, start: Int, end: Int, fuzz: Float): Boolean {
    for(i in start until (end + 1)) {
        if(fuzzEq(f, i.toFloat(), fuzz))
            return true
    }
    return false
}

fun noiseToOutline(f: Float): Int {
    val f1 = (f + 1) * 8
    val fuzz = 0.1f

    val col =
        if(nearInt(f1, 1, 8, fuzz))
            0
        else
            f1.toInt() * 32

    return (col and 0xff) * 0x010101
}

fun gen1DImage(grads: Gradients<Vector1>, perm: PermutationTable,
               width: Int, height: Int, subdivision: Float,
               normalize: Boolean,
               f: (Gradients<Vector1>, PermutationTable, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    val g2d = img.createGraphics()
    g2d.background = Color.white
    g2d.fillRect(0, 0, width, height)
    g2d.stroke = BasicStroke(3f)

    val half = height / 2
    val halfM10 = half - 10

    g2d.color = Color.red

    g2d.drawLine(0, half, width, half)

    var lx = 0
    var ly = 0

    var n = -1f
    if(normalize) {
        for (x in 0 until width) {
            val y = abs(f(grads, perm, x / subdivision))
            if(y > n) n = y
        }

        n = 1 / n
    }

    for (x in 0 until width) {
        val y = ((f(grads, perm, x / subdivision) * if(normalize) n else 1f) * halfM10).toInt() + half
        g2d.drawLine(lx, ly, x, y)
        if(x.rem(subdivision.toInt()) == 0) {
            g2d.color = Color.MAGENTA

            g2d.drawLine(x, half + 20, x, half - 20)

            g2d.color = Color.black
        }
        lx = x
        ly = y
    }

    return img
}

fun gen1DNoiseDistortionOverlay(grads: Gradients<Vector1>, perm: PermutationTable,
               width: Int, height: Int, subdivision: Float,
               normalize: Boolean, distortion: Float, offset: Float,
               f: (Gradients<Vector1>, PermutationTable, Float) -> Float): BufferedImage {
    val fp = distNoise(distortion, offset, f, vecNoise1(f))

    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    val g2d = img.createGraphics()
    g2d.background = Color.white
    g2d.fillRect(0, 0, width, height)
    g2d.stroke = BasicStroke(3f)

    val half = height / 2
    val halfM10 = half - 10

    g2d.color = Color.red

    g2d.drawLine(0, half, width, half)

    var lx = 0
    var dy = 0

    var py1 = 0

    var py2 = 0

    var dn = -1f
    var pn = -1f

    if(normalize) {
        for (x in 0 until width) {
            val y = abs(fp(grads, perm, x / subdivision))
            val py = abs(f( grads, perm, x / subdivision))
            if(y > dn) dn = y
            if(py > pn) pn = py
        }

        dn = 1 / dn
        pn = 1 / pn
    }

    for (x in 0 until width) {
        val y = ((fp(grads, perm, x / subdivision) * if(normalize) dn else 1f) * halfM10).toInt() + half
        val y1 = ((f(grads, perm, x / subdivision) * if(normalize) pn else 1f) * halfM10).toInt() + half
        val y2 = ((f(grads, perm, x / subdivision + offset) * if(normalize) pn else 1f) * halfM10).toInt() + half
        g2d.color = Color.black
        g2d.drawLine(lx, dy, x, y)
        g2d.color = Color.cyan
        g2d.drawLine(lx, py1, x, y1)
        g2d.color = Color.pink
        g2d.drawLine(lx, py2, x, y2)
        if(x.rem(subdivision.toInt()) == 0) {
            g2d.color = Color.MAGENTA

            g2d.drawLine(x, half + 20, x, half - 20)

            g2d.color = Color.black
        }
        lx = x
        dy = y
        py1 = y1
        py2 = y2
    }

    return img
}

fun gen2DImage(grads: Gradients<Vector2>, perm: PermutationTable,
               width: Int, height: Int, subdivision: Float,
               normalize: Boolean, grayscale: (Float) -> Int,
               f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    var n = -1f
    for (x in 0 until width)
        for (y in 0 until height) {
            val xd = x / subdivision
            val yd = y / subdivision
            val noise = abs(f(grads, perm, xd, yd))
            if(noise > n) n = noise
        }

    n = 1f / n

    //println(n)


    for (x in 0 until width)
        for (y in 0 until height) {
            val xd = x / subdivision
            val yd = y / subdivision

            val noise = f(grads, perm, xd, yd) * if(normalize) n else 1f
            val gray1 = grayscale(noise)
            img.setRGB(x, height - y - 1, gray1)
        }

    return img
}