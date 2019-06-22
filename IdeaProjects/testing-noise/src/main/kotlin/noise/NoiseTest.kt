package noise

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import java.nio.Buffer
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

fun noiseToGrayscale(f: Float): Int {
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

fun gen1DImage(grads: Gradients<Vector1>, perm: PermutationTable, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector1>, PermutationTable, Float) -> Float): BufferedImage {
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

    var normalize = -1f

    for (x in 0 until width) {
        val y = abs(f(grads, perm, x / subdivision))
        if(y > normalize) normalize = y
    }

    normalize = 1 / normalize

    for (x in 0 until width) {
        val y = (normalize * f(grads, perm, x / subdivision) * halfM10).toInt() + half
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

fun gen2DImage(grads: Gradients<Vector2>, perm: PermutationTable, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    /*var max = -1f

    for (x in 0 until width)
        for (y in 0 until height) {
            val xd = x / subdivision
            val yd = y / subdivision
            val (xs, ys) = unskew(xd, yd)

            val noise = abs(f(defaultPerlinGradients2, perm, xs, ys))
            if(noise > max) max = noise
        }

    val mul = 1f / max*/

    var max = -1f

    for (x in 0 until width)
        for (y in 0 until height) {
            val xd = x / subdivision
            val yd = y / subdivision
            val (xs, ys) = Pair(xd, yd)

            val noise = /*mul * */f(defaultPerlinGradients2, perm, xs, ys)
            if (abs(noise) > max) max = abs(noise)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, height - y - 1, gray1)
        }

    println(max)

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(1f)
    //g2d.font = Font(Font.MONOSPACED, Font.BOLD, 80)

    /*for (x in 0 until width step (subdivision).toInt()) {
        for (y in 0 until height step (subdivision).toInt()) {
            g2d.color = Color.red
            g2d.drawLine(x, height - y, x + (subdivision).toInt(), height - (y + (subdivision)).toInt()) //diagonal
            g2d.drawLine(x, height - y, x, height - (y + (subdivision)).toInt()) //vertical
            g2d.drawLine(x, height - y, (x + (subdivision)).toInt(), height - y) //horizontal

            //g2d.color = Color.yellow
            val c0 = (perm.perm[(x shr 2) and 0xff] xor perm.perm[(y shr 2) and 0xff]) and 0b11
            g2d.drawString("$c0", (x * subdivision).toInt() - 20, height - ((y * subdivision).toInt() - 20))
        }
    }*/

    return img
}

fun genSimplexUnskewedImage(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<Pair<TriangleTile, Int>>, width: Int, height: Int, subdivision: Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    //val img1 = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    var min = 2f
    var max = -2f

    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val (xt, yt) = unskew(xs, ys)

            val n = simplex(defaultPerlinGradients2, perm, xt, yt)
            if(n > max) {
                max = n
            }
            if(n < min) {
                min = n
            }
            val gray1 = noiseToOutline(n)
            img.setRGB(x, height - y - 1, gray1)
            //img1.setRGB(x, height - y - 1, gray1)
        }

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.font = Font(Font.MONOSPACED, Font.BOLD, 80)

    for (x in 0 until width step (subdivision * 4).toInt()) {
        for (y in 0 until height step (subdivision * 4).toInt()) {
            g2d.color = Color.red
            g2d.drawLine(x, height - y, x + (subdivision * 4).toInt(), height - (y + (subdivision * 4)).toInt()) //diagonal
            g2d.drawLine(x, height - y, x, height - (y + (subdivision * 4)).toInt()) //vertical
            g2d.drawLine(x, height - y, (x + (subdivision * 4)).toInt(), height - y) //horizontal

            //g2d.color = Color.yellow
            /*val c0 = (perm.perm[(x shr 2) and 0xff] xor perm.perm[(y shr 2) and 0xff]) and 0b11
            g2d.drawString("$c0", (x * subdivision).toInt() - 20, height - ((y * subdivision).toInt() - 20))*/
        }
    }

    for (x in 0 until (width / (subdivision).toInt())) {
        for (y in 0 until (height / (subdivision).toInt())) {
            g2d.color = Color.yellow
            val (x1, y1) =
                    if(x and 0b11 >= y and 0b11) {
                        Pair(y and 0b11, x and 0b11)
                    } else {
                        Pair(x and 0b11, y and 0b11)
                    }

            val c = ttile1[y1][x1]
            g2d.drawString("$c", (x * subdivision).toInt() - 20, height - ((y * subdivision).toInt() - 20))
        }
    }

    println("min: $min max: $max")

    return img
}

fun gen2DImageTile(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<SquareTile>, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, Array<SquareTile>, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    //println(perm)
    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val noise = f(defaultPerlinGradients2, perm, tiles, xs, ys)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, y, gray1)
        }

    return img
}

fun gen2DImageTileSimplex(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<TriangleTile>, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, Array<TriangleTile>, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    //println(perm)
    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val (xt, yt) = Pair(xs, ys)

            val noise = f(defaultPerlinGradients2, perm, tiles, xt, yt)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, height - y - 1, gray1)
        }

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.font = Font(Font.MONOSPACED, Font.BOLD, 60)

    for (x in 0 until width * 2 / subdivision.toInt() step 4) {
        for (y in 0 until height * 2 / subdivision.toInt() step 4) {
            val (x1, y1) = unskew(x.toFloat(), y.toFloat())
            val (x2, y2) = unskew(x.toFloat(), (y + 4).toFloat())
            val (x3, y3) = unskew((x + 4).toFloat(), y.toFloat())
            val (x4, y4) = unskew((x + 4).toFloat(), (y + 4).toFloat())


            g2d.color = Color.red
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x4 * subdivision).toInt(), height - (y4 * subdivision).toInt()) //diagonal
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x2 * subdivision).toInt(), height - (y2 * subdivision).toInt()) //vertical
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x3 * subdivision).toInt(), height - (y3 * subdivision).toInt()) //horizontal


            g2d.color = Color.yellow
            val c0 = (perm.perm[x shr 2 and 0xff] xor perm.perm[y shr 2 and 0xff]) and 0b11
            g2d.drawString("$c0", (x1 * subdivision).toInt(), height - (y1 * subdivision).toInt())
        }
    }

    return img
}

fun gen2DImageFractalTileSimplex(grads: Gradients<Vector2>, perm: PermutationTable, tiles: FractalTileSimplex, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, FractalTileSimplex, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    //println(perm)
    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val (xt, yt) = Pair(xs, ys)

            val noise = f(defaultPerlinGradients2, perm, tiles, xt, yt)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, height - y - 1, gray1)
        }

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.font = Font(Font.MONOSPACED, Font.BOLD, 60)

    for (x in 0 until width * 2 / subdivision.toInt() step 4) {
        for (y in 0 until height * 2 / subdivision.toInt() step 4) {
            val (x1, y1) = unskew(x.toFloat(), y.toFloat())
            val (x2, y2) = unskew(x.toFloat(), (y + 4).toFloat())
            val (x3, y3) = unskew((x + 4).toFloat(), y.toFloat())
            val (x4, y4) = unskew((x + 4).toFloat(), (y + 4).toFloat())


            g2d.color = Color.red
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x4 * subdivision).toInt(), height - (y4 * subdivision).toInt()) //diagonal
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x2 * subdivision).toInt(), height - (y2 * subdivision).toInt()) //vertical
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x3 * subdivision).toInt(), height - (y3 * subdivision).toInt()) //horizontal


            g2d.color = Color.yellow
            val c0 = (perm.perm[x shr 2 and 0xff] xor perm.perm[y shr 2 and 0xff]) and 0b11
            g2d.drawString("$c0", (x1 * subdivision).toInt(), height - (y1 * subdivision).toInt())
        }
    }

    return img
}

fun gen2DImageTileOutline(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<SquareTile>, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, Array<SquareTile>, Float, Float) -> Float): Pair<BufferedImage, BufferedImage> {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val img2 = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    //println(perm)
    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val noise = f(defaultPerlinGradients2, perm, tiles, xs, ys)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, height - y - 1, gray1)
            img2.setRGB(x, height - y - 1, gray1)
        }

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.font = Font(Font.MONOSPACED, Font.BOLD, 80)

    for (x in 0 until width step 4) {
        for (y in 0 until height step 4) {
            g2d.color = Color.red
            g2d.drawLine((x * subdivision).toInt(), height - (y * subdivision).toInt(), ((x + 4) * subdivision).toInt(), height - (y * subdivision).toInt())
            g2d.drawLine((x * subdivision).toInt(), height - (y * subdivision).toInt(), ((x) * subdivision).toInt(), height - ((y + 4) * subdivision).toInt())


            g2d.color = Color.yellow
            val c0 = (perm.perm[(x shr 2) and 0xff] xor perm.perm[(y shr 2) and 0xff]) and 0b11
            g2d.drawString("$c0", (x * subdivision).toInt() - 20, height - ((y * subdivision).toInt() - 20))
        }
    }

    return Pair(img, img2)
}

fun gen2DImageTileOutline(grads: Gradients<Vector2>, perm: PermutationTable, tiles: FractalTile, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, FractalTile, Float, Float) -> Float): Pair<BufferedImage, BufferedImage> {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val img2 = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    //println(perm)
    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val noise = f(defaultPerlinGradients2, perm, tiles, xs, ys)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, height - y - 1, gray1)
            img2.setRGB(x, height - y - 1, gray1)
        }

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.font = Font(Font.MONOSPACED, Font.BOLD, 80)

    for (x in 0 until width step 4) {
        for (y in 0 until height step 4) {
            g2d.color = Color.red
            g2d.drawLine((x * subdivision).toInt(), height - (y * subdivision).toInt(), ((x + 4) * subdivision).toInt(), height - (y * subdivision).toInt())
            g2d.drawLine((x * subdivision).toInt(), height - (y * subdivision).toInt(), ((x) * subdivision).toInt(), height - ((y + 4) * subdivision).toInt())


            g2d.color = Color.yellow
            val c0 = ((perm.perm[(x shr 2) and 0xff] xor perm.perm[(y shr 2) and 0xff])) and 0b11
            g2d.drawString("$c0", (x * subdivision).toInt() - 20, height - ((y * subdivision).toInt() - 20))
        }
    }

    return Pair(img, img2)
}



fun gen2DImageTile(grads: Gradients<Vector2>, perm: PermutationTable, tiles: Array<Pair<TriangleTile, Int>>, width: Int, height: Int, subdivision: Float, f: (Gradients<Vector2>, PermutationTable, Array<Pair<TriangleTile, Int>>, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    var max = -1f

    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val noise = f(defaultPerlinGradients2, perm, tiles, xs, ys)
            if(noise > max) max = noise
        }

    val mul = 1f / max

    println(mul)

    for (x in 0 until width)
        for (y in 0 until height) {
            val xs = x / subdivision
            val ys = y / subdivision

            val noise = mul * f(defaultPerlinGradients2, perm, tiles, xs, ys)
            val gray1 = noiseToOutline(noise)
            img.setRGB(x, height - y - 1, gray1)
        }

    val g2d = img.createGraphics()
    g2d.stroke = BasicStroke(3f)
    g2d.font = Font(Font.MONOSPACED, Font.PLAIN, 80)

    for (x in 0 until width step 4) {
        for (y in 0 until height step 4) {
            g2d.color = Color.red
            val (x0, y0) = unskew(x.toFloat(), y.toFloat())
            val (x1, y1) = unskew((x + 4).toFloat(), y.toFloat())
            val (x2, y2) = unskew((x + 4).toFloat(), (y + 4).toFloat())
            g2d.drawLine((x0 * subdivision).toInt(), height - (y0 * subdivision).toInt(), (x1 * subdivision).toInt(), height - (y1 * subdivision).toInt())
            g2d.drawLine((x1 * subdivision).toInt(), height - (y1 * subdivision).toInt(), (x2 * subdivision).toInt(), height - (y2 * subdivision).toInt())
            g2d.drawLine((x0 * subdivision).toInt(), height - (y0 * subdivision).toInt(), (x2 * subdivision).toInt(), height - (y2 * subdivision).toInt())

            g2d.color = Color.yellow
            val c0 = (perm.perm[(x shr 2) and 0xff] xor perm.perm[(y shr 2) and 0xff]) and 0x1
            g2d.drawString("$c0", (x0 * subdivision).toInt() - 20, height - ((y0 * subdivision).toInt() - 20))
        }
    }






    return img
}

class Buckets {
    val buckets = IntArray(20) {0}

    fun insert(f: Float) {
        if(f <= -0.9) buckets[0]++
        else if(f <= -0.8f) buckets[1]++
        else if(f <= -0.7f) buckets[2]++
        else if(f <= -0.6f) buckets[3]++
        else if(f <= -0.5f) buckets[4]++
        else if(f <= -0.4f) buckets[5]++
        else if(f <= -0.3f) buckets[6]++
        else if(f <= -0.2f) buckets[7]++
        else if(f <= -0.1f) buckets[8]++
        else if(f <= 0.0f) buckets[9]++
        else if(f <= 0.1f) buckets[10]++
        else if(f <= 0.2f) buckets[11]++
        else if(f <= 0.3f) buckets[12]++
        else if(f <= 0.4f) buckets[13]++
        else if(f <= 0.5f) buckets[14]++
        else if(f <= 0.6f) buckets[15]++
        else if(f <= 0.7f) buckets[16]++
        else if(f <= 0.8f) buckets[17]++
        else if(f <= 0.9f) buckets[18]++
        else buckets[19]++
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[-1.0 -0.9]: ${buckets[0]}\n")
        buffer.append("(-0.9 -0.8]: ${buckets[1]}\n")
        buffer.append("(-0.8 -0.7]: ${buckets[2]}\n")
        buffer.append("(-0.7 -0.6]: ${buckets[3]}\n")
        buffer.append("(-0.6 -0.5]: ${buckets[4]}\n")
        buffer.append("(-0.5 -0.4]: ${buckets[5]}\n")
        buffer.append("(-0.4 -0.3]: ${buckets[6]}\n")
        buffer.append("(-0.3 -0.2]: ${buckets[7]}\n")
        buffer.append("(-0.2 -0.1]: ${buckets[8]}\n")
        buffer.append("(-0.1 0.0]: ${buckets[9]}\n")
        buffer.append("(0.0 0.1]: ${buckets[10]}\n")
        buffer.append("(0.1 0.2]: ${buckets[11]}\n")
        buffer.append("(0.2 0.3]: ${buckets[12]}\n")
        buffer.append("(0.3 0.4]: ${buckets[13]}\n")
        buffer.append("(0.4 0.5]: ${buckets[14]}\n")
        buffer.append("(0.5 0.6]: ${buckets[15]}\n")
        buffer.append("(0.6 0.7]: ${buckets[16]}\n")
        buffer.append("(0.7 0.8]: ${buckets[17]}\n")
        buffer.append("(0.8 0.9]: ${buckets[18]}\n")
        buffer.append("(0.9 1.0]: ${buckets[19]}")
        return buffer.toString()
    }
}

class ZeroCrossingBuckets {
    val buckets = IntArray(20){0}

    fun insert(f: Float) {
        buckets[(f * 39.9999f).toInt()]++
    }

    fun add(z: ZeroCrossingBuckets) {
        for(i in 0 until 20)
            buckets[i] += z.buckets[i]
    }
}

fun testSimplexZeroCrossings(width: Int, height: Int, subdivision: Int, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): String {
    val buckets1 = Buckets()
    val buckets2 = Buckets()
    val buckets3 = Buckets()
    val buckets4 = Buckets()
    val buckets5 = Buckets()
    val bucketsat = Buckets()

    val times = 1000

    var max = -1f
    var max2 = -1f

    for(i in 0 until times) {
        val ptest = perm(System.nanoTime())
        val grads = genPerlinGrads(128, Random(System.nanoTime()))

        for(y in -height until height) {
            for(x in -width until width) {
                val xf = x / subdivision.toFloat()
                val yf = y / subdivision.toFloat()


                val F2 = 0.5f * (sqrt(3f)-1f)
                val s = (xf + yf) * F2
                val i = ffloor(xf + s)
                val j = ffloor(yf + s)

                val G2 = (3f - sqrt(3f))/6f
                val t = (i + j) * G2

                val X0 = i - t
                val Y0 = j - t
                val x0 = xf - X0
                val y0 = yf - Y0

                var i1: Int
                var j1: Int
                if(x0 > y0) {
                    i1 = 1
                    j1 = 0
                } else {
                    i1 = 0
                    j1 = 1
                }

                val x1 = x0 - i1 + G2
                val y1 = y0 - j1 + G2
                val x2 = x0 - 1f + 2f * G2
                val y2 = y0 - 1f + 2f * G2

                val dst_0 = x0 * x0 + y0 * y0
                val dst_1 = x1 * x1 + y1 * y1
                val dst_2 = x2 * x2 + y2 * y2
                val dst2 = min(dst_0, min(dst_1, dst_2))

                val n = f(grads, ptest, xf, yf)

                if(abs(n) > max) max = abs(n)
                if(dst2 > max2) max2 = dst2

                if(dst2 <= 0.044f) buckets1.insert(n)
                else if(dst2 <= 0.088f) buckets2.insert(n)
                else if(dst2 <= 0.132f) buckets3.insert(n)
                else if(dst2 <= 0.176f) buckets4.insert(n)
                else if(dst2 <= 0.22f) buckets5.insert(n)

                if((y % subdivision == 0) and (x % subdivision == 0))
                    bucketsat.insert(n)
            }
        }
    }

    println(max)
    println(max2)

    val buffer = StringBuffer()

    for(i in 0 until 20)
        buffer.append(",${buckets1.buckets[i] / times.toFloat()},${buckets2.buckets[i] / times.toFloat()},${buckets3.buckets[i] / times.toFloat()},${buckets4.buckets[i] / times.toFloat()},${buckets5.buckets[i] / times.toFloat()}\n")
    return buffer.toString()
}

fun test2DZeroCrossings(width: Int, height: Int, subdivision: Int, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): String {
    val buckets1 = Buckets()
    val buckets2 = Buckets()
    val buckets3 = Buckets()
    val buckets4 = Buckets()
    val buckets5 = Buckets()
    val bucketsat = Buckets()

    val times = 1000

    var max = -1f

    for(i in 0 until times) {
        val ptest = perm(System.nanoTime())
        val grads = genPerlinGrads(128, Random(System.nanoTime()))

        for(y in -height until height) {
            for(x in -width until width) {
                val xf = x / subdivision.toFloat()
                val yf = y / subdivision.toFloat()

                val xr = xf.roundToInt()
                val yr = yf.roundToInt()
                val xdst = xf - xr
                val ydst = yf - yr
                val dst2 = xdst * xdst + ydst * ydst

                val n = f(grads, ptest, xf, yf)

                if(abs(n) > max) max = abs(n)

                if(dst2 <= 0.1f) buckets1.insert(n)
                else if(dst2 <= 0.2f) buckets2.insert(n)
                else if(dst2 <= 0.3f) buckets3.insert(n)
                else if(dst2 <= 0.4f) buckets4.insert(n)
                else if(dst2 <= 0.5f) buckets5.insert(n)

                if((y % subdivision == 0) and (x % subdivision == 0))
                    bucketsat.insert(n)
            }
        }
    }

    println(max)

    val buffer = StringBuffer()

    for(i in 0 until 20)
        buffer.append("${bucketsat.buckets[i] / times.toFloat()},${buckets1.buckets[i] / times.toFloat()},${buckets2.buckets[i] / times.toFloat()},${buckets3.buckets[i] / times.toFloat()},${buckets4.buckets[i] / times.toFloat()},${buckets5.buckets[i] / times.toFloat()}\n")
    return buffer.toString()
}

fun test1DZeroCrossing(grads: Gradients<Vector1>, perm: PermutationTable, width: Int, subdivision: Int, f: (Gradients<Vector1>, PermutationTable, Float) -> Float): Triple<Int, ZeroCrossingBuckets, String> {
    val buckets1 = Buckets()
    val buckets2 = Buckets()
    val buckets3 = Buckets()
    val buckets4 = Buckets()
    val buckets5 = Buckets()
    val bucketsat = Buckets()

    val zerocrossings = ZeroCrossingBuckets()

    var max = -1f
    var min = 1f

    var last = -20f
    var zeroes = 0
    val times = 10000
    for(i in 0 until times) {
        val ptest = perm(System.nanoTime())
        val rand = Random(System.nanoTime())
        val testPerlinGradients =
            Gradients(
                Array<Vector1>(128) { Vector1(rand.nextFloat() * 2f - 1f) }
            )

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
            if(last != -20f) {
                if(((last < 0.0f) and (0.0f <= y)) or ((y <= 0.0f) and (0.0f < last))) {
                    zerocrossings.insert(xs)
                    zeroes++
                }
            }

            last = y

            if(xs <= 0.1f) buckets1.insert(y)
            else if(xs <= 0.2f) buckets2.insert(y)
            else if(xs <= 0.3f) buckets3.insert(y)
            else if(xs <= 0.4f) buckets4.insert(y)
            else if(xs <= 0.5f) buckets5.insert(y)

            if (y > max) max = y
            if (y < min) min = y

            if (x.rem(subdivision) == 0) {
                bucketsat.insert(y)
            }
        }
    }

    //println("$min $max $zeroes")

    val buffer = StringBuffer()

    for(i in 0 until 20)
        buffer.append("${bucketsat.buckets[i] / times.toFloat()},${buckets1.buckets[i] / times.toFloat()},${buckets2.buckets[i] / times.toFloat()},${buckets3.buckets[i] / times.toFloat()},${buckets4.buckets[i] / times.toFloat()},${buckets5.buckets[i] / times.toFloat()}\n")
    //return buffer.toString()

    return Triple(zeroes, zerocrossings, buffer.toString())
}

fun skew(x: Float, y: Float): Pair<Float, Float> {
    val G2 = 0.5f * (sqrt(3f)-1f)
    val t = (x + y) * G2
    return Pair(x + t, y + t)
}

fun unskew(x: Float, y: Float): Pair<Float, Float> {
    val G2 = (3f - sqrt(3f))/6f
    val t = (x + y) * G2
    return Pair(x - t, y - t)
}

fun simplex2Vertices() {
    val G2 = (3f - sqrt(3f))/6f
    val i1 = 0
    val j1 = 1

    val (x0, y0) = unskew(0.5f, 0.5f)
    println(Pair(x0, y0))
    val (x1, y1) = Pair(x0 - i1 + G2, y0 - j1 + G2)
    println(skew(x1, y1))
    val (x2, y2) = Pair(x1 - i1 + G2, y1 - j1 + G2)
    println(skew(x2, y2))
    val (x3, y3) = Pair(x0 + j1 - G2, y0 + i1 - G2)
    println(skew(x3, y3))
    val (x4, y4) = Pair(x1 + j1 - G2, y1 + i1 - G2)
    println(skew(x4, y4))
    val (x5, y5) = Pair(x0 + i1 - G2, y0 + j1 - G2)
    println(skew(x5, y5))
    val (x6, y6) = Pair(x0 - j1 + G2, y0 - i1 + G2)
    println(skew(x6, y6))
    val (x7, y7) = Pair(x0 + 1f - 2f * G2, y0 + 1f - 2f * G2)
    println(skew(x7, y7))
    val (x8, y8) = Pair(x0 - 1f + 2f * G2, y0 - 1f + 2f * G2)
    println(skew(x8, y8))
    val (x9, y9) = Pair(x8 - i1 + G2, y8 - j1 + G2)
    println(skew(x9, y9))
    val (x10, y10) = Pair(x8 - j1 + G2, y8 - i1 + G2)
    println(skew(x10, y10))
    val (x11, y11) = Pair(x8 - 1f + 2f * G2, y8 - 1f + 2f * G2)
    println(skew(x11, y11))
    val (x12, y12) = Pair(x0 - 2f + 4f * G2, y0 - 2f + 4f * G2)
    println(skew(x12, y12))
}

fun count2DZeroCrossings(grads: Gradients<Vector2>, perm: PermutationTable, width: Int, height: Int, subdivision: Int, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): Long {
    var count = 0L

    for(x in 0 until width) {
        for(y in 0 until height) {
            val x1 = x / subdivision.toFloat()
            val y1 = y / subdivision.toFloat()
            val z = f(grads, perm, x1, y1)
            loop@ for(x0 in -1 until 2) {
                for(y0 in -1 until 2) {
                    if((x0 != 0) and (y0 != 0)) {
                        val x2 = (x + x0) / subdivision.toFloat()
                        val y2 = (y + y0) / subdivision.toFloat()
                        val z2 = f(grads, perm, x2, y2)
                        if(((z2 <= 0f) and (0f <= z)) or ((z <= 0f) and (0f <= z2))) {
                            count++
                            break@loop
                        }
                    }
                }
            }
        }
    }

    return count
}

fun draw2DZeroCrossings(grads: Gradients<Vector2>, perm: PermutationTable, width: Int, height: Int, subdivision: Int, f: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for(x in 0 until width) {
        for(y in 0 until height) {
            val x1 = x / subdivision.toFloat()
            val y1 = y / subdivision.toFloat()
            val z = f(grads, perm, x1, y1)
            img.setRGB(x, height - y - 1, 0xffffff)
            loop@ for(x0 in -1 until 2) {
                for(y0 in -1 until 2) {
                    if((x0 != 0) and (y0 != 0)) {
                        val x2 = (x + x0) / subdivision.toFloat()
                        val y2 = (y + y0) / subdivision.toFloat()
                        val z2 = f(grads, perm, x2, y2)
                        if(((z2 <= 0f) and (0f <= z)) or ((z <= 0f) and (0f <= z2))) {
                            img.setRGB(x, height - y - 1, 0x000000)
                            break@loop
                        }
                    }
                }
            }
        }
    }

    return img
}

fun reposeDown(cells: Array<IntArray>, x: Int, y: Int, angle: Float) {
    var minValue = Int.MAX_VALUE
    var minx = 0
    var miny = 0

    if(y + 1 < 512 && cells[y + 1][x] < minValue) {
        minValue = cells[y + 1][x]
        minx = x
        miny = y + 1
    }
    if(y - 1 >= 0 && cells[y - 1][x] < minValue) {
        minValue = cells[y - 1][x]
        minx = x
        miny = y - 1
    }
    if(x + 1 < 512 && cells[y][x + 1] < minValue) {
        minValue = cells[y][x + 1]
        minx = x + 1
        miny = y
    }
    if(x - 1 >= 0 && cells[y][x - 1] < minValue) {
        minValue = cells[y][x - 1]
        minx = x - 1
        miny = y
    }

    var doMore = false

    while(cells[y][x] > 3 + cells[miny][minx]) {
        cells[y][x]--
        cells[miny][minx]++
        doMore = true
    }

    if(doMore)
        reposeDown(cells, minx, miny, angle)
}

fun reposeUp(cells: Array<IntArray>, x: Int, y: Int, angle: Float) {
    var maxValue = Int.MIN_VALUE
    var maxx = 0
    var maxy = 0

    if(y + 1 < 512 && cells[y + 1][x] > maxValue) {
        maxValue = cells[y + 1][x]
        maxx = x
        maxy = y + 1
    }
    if(y - 1 >= 0 && cells[y - 1][x] > maxValue) {
        maxValue = cells[y - 1][x]
        maxx = x
        maxy = y - 1
    }
    if(x + 1 < 512 && cells[y][x + 1] > maxValue) {
        maxValue = cells[y][x + 1]
        maxx = x + 1
        maxy = y
    }
    if(x - 1 >= 0 && cells[y][x - 1] > maxValue) {
        maxValue = cells[y][x - 1]
        maxx = x - 1
        maxy = y
    }

    var doMore = false

    while(cells[maxy][maxx] > cells[y][x] + 3) {
        //println("Reposed up from ($maxx, $maxy) to ($x, $y)")
        doMore = true
        cells[y][x]++
        cells[maxy][maxx]--
    }

    if (doMore) {
        //println("reposeUp")
        reposeUp(cells, maxx, maxy, angle)
    }
}

fun draw9Timescale(cells: Array<IntArray>, img: BufferedImage, loc: Int) {
    val x = loc % 3
    val y = loc / 3

    for(yp in 0 until 512) {
        for(xp in 0 until 512) {
            //if(cells[y][x] > max) max = cells[y][x]
            //println(cells[y][x])
            if(cells[yp][xp] < 10) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 24))
            else if(cells[yp][xp] < 20) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 48))
            else if(cells[yp][xp] < 30) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 72))
            else if(cells[yp][xp] < 40) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 96))
            else if(cells[yp][xp] < 50) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 120))
            else if(cells[yp][xp] < 60) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 144))
            else if(cells[yp][xp] < 70) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 168))
            else if(cells[yp][xp] < 80) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 192))
            else if(cells[yp][xp] < 90) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 216))
            else if(cells[yp][xp] < 100) img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * (255 - 240))
            else img.setRGB(xp + (x * 512) + ((x + 1) * 3), (512 * 3 + 11) - ((y + 1) * 3) - (y * 512) - (512 - yp), 0x010101 * 1)
        }
    }
}

fun drawCells(cells: Array<IntArray>, img: BufferedImage) {
    for(y in 0 until 512) {
        for(x in 0 until 512) {
            //if(cells[y][x] > max) max = cells[y][x]
            //println(cells[y][x])
            if(cells[y][x] < 10) img.setRGB(x, 511 - y, 0x010101 * (255 - 24))
            else if(cells[y][x] < 20) img.setRGB(x, 511 - y, 0x010101 * (255 - 48))
            else if(cells[y][x] < 30) img.setRGB(x, 511 - y, 0x010101 * (255 - 72))
            else if(cells[y][x] < 40) img.setRGB(x, 511 - y, 0x010101 * (255 - 96))
            else if(cells[y][x] < 50) img.setRGB(x, 511 - y, 0x010101 * (255 - 120))
            else if(cells[y][x] < 60) img.setRGB(x, 511 - y, 0x010101 * (255 - 144))
            else if(cells[y][x] < 70) img.setRGB(x, 511 - y, 0x010101 * (255 - 168))
            else if(cells[y][x] < 80) img.setRGB(x, 511 - y, 0x010101 * (255 - 192))
            else if(cells[y][x] < 90) img.setRGB(x, 511 - y, 0x010101 * (255 - 216))
            else if(cells[y][x] < 100) img.setRGB(x, 511 - y, 0x010101 * (255 - 240))
            else img.setRGB(x, 511 - y, 0x010101 * 1)
        }
    }
}

fun generateDunes(grads: Gradients<Vector2>, perm: PermutationTable, freq: Float,
                  l: Int, dir: Vector2, angle: Float, plower: Int, phigher: Int,
                  t: Int, times: IntArray,
                  noise: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float): BufferedImage {
    val rand = Random(System.nanoTime())

    //println(freq)

    val img = BufferedImage(512 * 3 + 12, 512 * 3 + 12, BufferedImage.TYPE_INT_RGB)
    for(y in 0 until 512 * 3 + 12) {
        for(x in 0 until 512 * 3 + 12) {
            img.setRGB(x, y, 0xff0000)
        }
    }

    val cells = Array(512) {
        y ->
        IntArray(512) {
            x ->
            val n = ((noise(grads, perm, x * freq, y * freq) + 1f) * 25f)
            n.roundToInt()
        }
    }

    var index = 0
    //val images = Array(files.size) {BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB)}

    for(i in 0 until t) {
        val xo = rand.nextInt(512)
        val yo = rand.nextInt(512)

        var x = xo
        var y = yo

        var xf = x.toFloat()
        var yf = y.toFloat()

        if (cells[y][x] > 0) {
            cells[y][x]--
            //println("\nRemoved from ($x, $y)")
        }

        while(true) {
            xf += dir.x * l
            yf += dir.y * l

            if(xf >= 512f) xf -= 512f
            if(yf >= 512f) yf -= 512f

            val xp = xf.roundToInt()
            val yp = yf.roundToInt()

            val p = rand.nextInt(1000)

            if(cells[yp][xp] < cells[y][x]) {
                if(p < plower) {
                    //println("Placed at ($xp, $yp)")
                    cells[yp][xp]++
                    reposeDown(cells, xp, yp, angle)
                    break
                }
            } else {
                if(p < phigher) {
                    //println("Placed at ($xp, $yp)")
                    cells[yp][xp]++
                    reposeDown(cells, xp, yp, angle)
                    break
                }
            }

            x = xp
            y = yp
        }

        reposeUp(cells, xo, yo, angle)
        if(i == times[index]) {
            draw9Timescale(cells, img, index)
            //ImageIO.write(images[index], "png", files[index])
            index++
        }
    }

    return img
}

fun updateShadowTable(shadowTable: Array<IntArray>, cells: Array<IntArray>, x: Int, y: Int) {
    if(shadowTable[y][x] == -1) { //not in shadow
        if(shadowTable[y][x-1] == -1) { //previous cell not in shadow

        } else { //previous cell in shadow
            if(shadowTable[y][x - 1] / 4 >= cells[x][y]) { //in shadow of previous peak
                shadowTable[y][x] = shadowTable[y][x-1]-1
            } else { //not in shadow of previous peak

            }
        }
    } else { //in shadow

    }
}

fun generateDunesV2(grads: Gradients<Vector2>, perm: PermutationTable, freq: Float,
                    l: Int, dir: Vector2, angle: Float, pns: Int, ps: Int,
                    t: Int, times: IntArray,
                    noise: (Gradients<Vector2>, PermutationTable, Float, Float) -> Float) {
    val rand = Random(System.nanoTime())

    val cells = Array(512) {
            y ->
        IntArray(512) {
                x ->
            val n = ((noise(grads, perm, x * freq, y * freq) + 1f) * 25f)
            n.roundToInt()
        }
    }

    val shadowTable = Array(512) { IntArray(512) { 0 } }

    for(y in 0 until 512) {
        for(x in 0 until 512) {
            if(x != 0) {
                if(shadowTable[y][x-1] == -1) { //previous cell is not in shadow
                    if(cells[y][x-1] > cells[y][x]) cells[y][x-1]*4-1 //in shadow of previous cell
                    else -1 //not in shadow of previous cell
                } else { //previous cell is in shadow
                    if(shadowTable[y][x-1]/4>=cells[y][x]) shadowTable[y][x-1]-1 //in shadow of previous peak
                    else -1 //not in shadow of previous peak
                }
            } else {
                -1
            }
        }
    }

    for(c in 0 until t) {
        var i = rand.nextInt(512)
        var j = rand.nextInt(512)

        while((cells[j][i] != 0) and (shadowTable[j][i] != -1)) {
            i = rand.nextInt(512)
            j = rand.nextInt(512)
        }

        val io = i
        val jo = j

        cells[j][i]--

        while(true) {
            val ip = i + (dir.x * l).toInt()
            val jp = j + (dir.y * l).toInt()

            if(shadowTable[jp][ip] != -1) { // in shadow, always place sand
                cells[jp][ip]++

            }
        }
    }
}

fun main(args: Array<String>) {
    //simplex2Vertices()
    val p = perm(System.nanoTime())
    val iwidth = 128 * 16
    val iheight = 128 * 16
    val isubdivision = 128f

    val width = 64 * 16
    val height = 64 * 16
    val subdivision = 48
    println(unskew(1f, 0f))
/*
    val testIntervals = 32
    val zerosProduct = LongArray(testIntervals) {0}

    val bucketPerlin = Buckets()
    val bucketPerlin2 = Buckets()

    val bucketSimplex = Buckets()
    val bucketSimplex2 = Buckets()*/

    /*for(i in 0 until 100) {
        val ptest = perm(System.nanoTime())
        for(x in 0 until 1000) {
            for(y in 0 until 1000) {
                val (x1, y1) = unskew(x.toFloat(), y.toFloat())
                val p = perlin(defaultPerlinGradients2, ptest, x.toFloat(), y.toFloat())
                val p2 = perlinRadial2v2(defaultPerlinGradients2, ptest, x.toFloat(), y.toFloat())
                val s = simplex(defaultPerlinGradients2, ptest, x1, y1)
                val s2 = simplex3v2(defaultPerlinGradients2, ptest, x1, y1)
                bucketSimplex.insert(s)
                bucketPerlin.insert(p)
                bucketPerlin2.insert(p2)
                bucketSimplex2.insert(s2)
            }
        }
    }

    val buffer = StringBuffer()

    for(i in 0 until 20)
        buffer.append("${bucketPerlin.buckets[i] / 100f}, ${bucketPerlin2.buckets[i] / 100f}, ${bucketSimplex.buckets[i] / 100f}, ${bucketSimplex2.buckets[i] / 100f}\n")*/

    /*for(testNum in 0 until 10) {

        val ptest = perm(System.nanoTime())
        val simplexZeroes = count2DZeroCrossings(defaultPerlinGradients2, ptest, width, height, subdivision, ::simplex)

        for (i in (testIntervals / 4) until (testIntervals / 2)) {
            println("$testNum $i")
            val zeroes = count2DZeroCrossings(defaultPerlinGradients2, ptest, width, height, subdivision, {g1, p1, x1, y1 -> simplex3v2(g1, p1, x1 * (1f + i / testIntervals.toFloat()), y1 * (1f + i / testIntervals.toFloat()))})
            zerosProduct[i] += abs(simplexZeroes - zeroes)
        }
    }

    var minIndex = (testIntervals / 4)

    for(i in ((testIntervals / 4) + 1) until (testIntervals / 2)) {
        println(zerosProduct[i])
        if(zerosProduct[i] < zerosProduct[minIndex]) {
            minIndex = i
        }
    }

    println("${1f + (minIndex / testIntervals.toFloat())}")*/

    //println(unskew(r2, r2))

    /*var max = -1f
    for(z in 0 until 5) {
        val p = perm(System.nanoTime())

        for(x in 0 until width) {
            for(y in 0 until height) {
                val x1 = x / subdivision.toFloat()
                val y1 = y / subdivision.toFloat()
                val n = abs(simplex(defaultPerlinGradients2, p, x1, y1))
                if(n > max) max = n
            }
        }
    }

    println(max)*/


    /*val (x0, y0) = unskew(2.751f, 2.75f)
    val (x1, y1) = unskew(2.75f, 2.751f)

    println(skew(x0, y0))
    println(skew(x1, y1))

    simplex2(defaultPerlinGradients2, p, x0, y0, 0)
    simplex2(defaultPerlinGradients2, p, x1, y1, 0)*/
    //1-d test
    //val oneDFunctions = arrayOf<(Gradients<Vector1>, PermutationTable, Float) -> Float>(::perlin, {g, p, x -> fractal(g, p, x, 2)}, ::perlinRadial2, ::perlinRadial2v2, ::perlinRadial2v3, ::perlinRadial3, ::perlinRadial4)
    //val oneDFunctions = arrayOf<(Gradients<Vector1>, PermutationTable, Float) -> Float>(::perlinRadial2v2, { g, p1, x -> perlinRadial2v2(g, p1, x * 1.5f )}, { g, p1, x -> perlinRadial2v2(g, p1, x * 1.75f )}, { g, p1, x -> perlinRadial2v2(g, p1, x * 2.0859375f )}, { g, p1, x -> perlinRadial2v2(g, p1, x * 2.25f )}, { g, p1, x -> perlinRadial2v2(g, p1, x * 2.5f )})

    //val oneDFunctions = arrayOf<(Gradients<Vector1>, PermutationTable, Float) -> Float>(::perlinRadial2)

    /*val testIntervals = 32

    val zerosProduct = LongArray(testIntervals) {1}
    val zerosProduct2 = LongArray(testIntervals) {1}

    val zeroCrossings = Array(testIntervals){ZeroCrossingBuckets()}
    val zeroCrossings2 = Array(testIntervals){ZeroCrossingBuckets()}
    val perlinZeroCrossings = ZeroCrossingBuckets()

    for(testNum in 0 until 30) {
        println(testNum)
        val start = System.currentTimeMillis()
        val ptest = perm(System.nanoTime())
        val rand = Random(System.nanoTime())
        val testPerlinGradients =
            Gradients(
                Array<Vector1>(128) { Vector1(rand.nextFloat() * 2f - 1f) }
            )
        val (perlinZeroes, perlinCrossings) = test1DZeroCrossing(testPerlinGradients, ptest, width, subdivision, ::perlin)
        perlinZeroCrossings.add(perlinCrossings)

        for (i in 0 until testIntervals) {
            val (zeroes, crossings) = test1DZeroCrossing(testPerlinGradients, ptest, width, subdivision, { g, p1, x -> perlinRadial2(g, p1, x * (1.5f + i / testIntervals.toFloat())) })
            val (zeroes2, crossings2) = test1DZeroCrossing(testPerlinGradients, ptest, width, subdivision, { g, p1, x -> perlinRadial2v2(g, p1, x * (1.5f + i / testIntervals.toFloat())) })
            zeroCrossings[i].add(crossings)
            zeroCrossings2[i].add(crossings2)

            zerosProduct[i] += abs(perlinZeroes - zeroes).toLong()
            zerosProduct2[i] += abs(perlinZeroes - zeroes2).toLong()
        }
        val ellapsed = (System.currentTimeMillis() - start) / 1000f
        val secondsRemaining = (30 - 1 - testNum) * ellapsed
        println("Estimated time remaining: ${secondsRemaining / 60}")
    }

    val zeroesBuffer = StringBuffer()


    var minIndex = 0
    var minIndex2 = 0

    zeroesBuffer.append("${zerosProduct[0]}, ${zerosProduct2[0]}\n")
    for(i in 1 until testIntervals) {
        zeroesBuffer.append("${zerosProduct[i]}, ${zerosProduct2[i]}\n")
        if(zerosProduct[i] < zerosProduct[minIndex]) {
            minIndex = i
        }
        if(zerosProduct2[i] < zerosProduct2[minIndex2]) {
            minIndex2 = i
        }
    }

    println("${1.5f + (minIndex / testIntervals.toFloat())}")
    println("${1.5f + (minIndex2 / testIntervals.toFloat())}")

    val buffer = StringBuffer()
    val buffer2 = StringBuffer()
    val buffer3 = StringBuffer()

    for(c in 0 until testIntervals) {
        var min = 0
        var min2 = 0
        var average = 0
        var average2 = 0
        for (i in 0 until 20) {
            if(zeroCrossings[c].buckets[i] > zeroCrossings[c].buckets[min]) {
                min = i
            }
            if(zeroCrossings2[c].buckets[i] > zeroCrossings2[c].buckets[min2]) {
                min2 = i
            }0.22222221
            average += zeroCrossings[c].buckets[i]
            average2 += zeroCrossings2[c].buckets[i]
            buffer.append("${zeroCrossings[c].buckets[i]},")
            buffer2.append("${zeroCrossings2[c].buckets[i]},")
        }
        average /= 20
        average2 /= 20
        var deviation = 0f
        var deviation2 = 0f
        for(i in 0 until 20) {
            deviation += abs(zeroCrossings[c].buckets[i] - average)
            deviation2 += abs(zeroCrossings2[c].buckets[i] - average2)
        }
        buffer.append("\n")
        buffer2.append("\n")
        buffer3.append("${zeroCrossings[c].buckets[min]},${zeroCrossings2[c].buckets[min2]},$deviation,$deviation2,${deviation / 20f},${deviation2 / 20f}\n")
    }

    val buffer4 = StringBuffer()
    for(i in 0 until 20) {
        buffer4.append("${perlinZeroCrossings.buckets[i]}\n")
    }*/


    /*val oneDFunctions = arrayOf<(Gradients<Vector1>, PermutationTable, Float) -> Float>(
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2f, ::perlin)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2.11f, ::perlin)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2.17f, ::perlin)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2.23f, ::perlin)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2f, ::perlinRadial2v2)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2.11f, ::perlinRadial2v2)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2.17f, ::perlinRadial2v2)},
        {g1, p1, x1 -> fractal(g1, p1, x1, 5, 2.23f, ::perlinRadial2v2)}
    )*/
    //val texts1 = oneDFunctions.map {test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, it)}
    //val images1 = oneDFunctions.map {gen1DImage(defaultPerlinGradients1, p, iwidth, iheight, isubdivision, it)}
    //2-d test
    //val twoDFunctions = arrayOf<(Gradients<Vector2>, PermutationTable, Float, Float) -> Float>(::perlin, ::simplex, ::perlinRadial2, ::perlinRadial2v2, ::simplex3, ::simplex3v2)
    /*val twoDFunctions = arrayOf<(Gradients<Vector2>, PermutationTable, Float, Float) -> Float>(
        ::perlin,
        ::simplex,
        ::perlinRadial2v2,
        ::simplex3v2)*/
    //val images2 = twoDFunctions.map { gen2DImage(defaultPerlinGradients2, p, iwidth, iheight, isubdivision, it) }
    //val images2v2 = twoDFunctions.map { draw2DZeroCrossings(defaultPerlinGradients2, p, iwidth, iheight, subdivision, it) }

    try {
        val text0 = File("/home/mark/noisestuff/text0.csv")
        val text1 = File("/home/mark/noisestuff/text1.csv")
        if(text0.exists()) text0.delete()
        if(text1.exists()) text1.delete()
        text0.writeText(testSimplexZeroCrossings(width, height, subdivision, ::simplex))
        text1.writeText(testSimplexZeroCrossings(width, height, subdivision, ::simplex3v2))

        /*for(i in 0 until texts1.size) {
            //val imageFile = File("/home/mark/noisestuff/1d-img$i.png")
            val textFile = File("/home/mark/noisestuff/text$i.csv")
            //ImageIO.write(images1[i], "png", imageFile)
            if(textFile.exists()) textFile.delete()
            textFile.writeText(texts1[i])
        }*/

        /*val textFile = File("/home/mark/noisestuff/text.csv")
        textFile.writeText(buffer.toString())*/

        /*for(i in 0 until twoDFunctions.size) {
            val imageFile = File("/home/mark/noisestuff/2d-img$i.png")
            ImageIO.write(images2[i], "png", imageFile)
        }

        for(i in 0 until twoDFunctions.size) {
            val imageFile = File("/home/mark/noisestuff/2d-img${i + twoDFunctions.size}.png")
            ImageIO.write(images2v2[i], "png", imageFile)
        }*/

        /*val f0 = File("/home/mark/noisestuff/2d-img0.png")
        val img = draw2DZeroCrossings(defaultPerlinGradients2, p, iwidth, iheight, subdivision, ::simplex)
        ImageIO.write(img, "png", f0)*/
        /*val f0 = File("/home/mark/noisestuff/img0.png")
        val f1 = File("/home/mark/noisestuff/img1.png")
        val f2 = File("/home/mark/noisestuff/img2.png")
        ImageIO.write(generateDunes(defaultPerlinGradients2, p, 4/512f, 5, Vector2(1f, 0f), .3f, 400, 600, 512 * 512 * 1, ::perlin), "png", f0)
        ImageIO.write(generateDunes(defaultPerlinGradients2, p, 4/512f, 5, Vector2(1f, 0f), .3f, 400, 600, 512 * 512 * 500, ::perlin), "png", f1)
        ImageIO.write(generateDunes(defaultPerlinGradients2, p, 4/512f, 5, Vector2(1f, 0f), .3f, 400, 600, 512 * 512 * 1000, ::perlin), "png", f2)*/
        /*val f0 = File("/home/mark/noisestuff/img0.png")
        val img = generateDunes(defaultPerlinGradients2, p, 4/512f, 5, Vector2(1f, 0f), .3f, 400, 600, 512 * 512 * 4000 + 1,
            intArrayOf(
                512 * 512,
                512 * 512 * 500,
                512 * 512 * 1000,
                512 * 512 * 1500,
                512 * 512 * 2000,
                512 * 512 * 2500,
                512 * 512 * 3000,
                512 * 512 * 3500,
                512 * 512 * 4000), ::perlin)
        ImageIO.write(img, "png", f0)*/
        //ImageIO.write(generateDunes(defaultPerlinGradients2, p, 16/512f, 3, Vector2(1f, 0f), .3f, 200, 400, /*512 * 512*/ 200, 200, ::perlinRadial2v2), "png", f1)
        //ImageIO.write(generateDunes(defaultPerlinGradients2, p, 16/512f, 3, Vector2(1f, 0f), .3f, 200, 400, /*512 * 512*/ 200, 200, ::simplex3v2), "png", f2)
        //f0.writeText(buffer.toString())
        /*val f0 = File("/home/mark/noisestuff/text0.csv")
        val f1 = File("/home/mark/noisestuff/text1.csv")
        val f2 = File("/home/mark/noisestuff/text2.csv")
        val f3 = File("/home/mark/noisestuff/text3.csv")
        val f4 = File("/home/mark/noisestuff/text4.csv")
        val f5 = File("/home/mark/noisestuff/text5.csv")
        val f6 = File("/home/mark/noisestuff/text6.csv")
        val f7 = File("/home/mark/noisestuff/text7.csv")
        val f8 = File("/home/mark/noisestuff/text8.csv")
        val buffer = StringBuffer()
        val (perlinZeroes1, perlinZeroCrossings1, perlinBuckets1) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2f, ::perlin)})
        println("1")
        val (perlinZeroes2, perlinZeroCrossings2, perlinBuckets2) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2.11f, ::perlin)})
        println("2")
        val (perlinZeroes3, perlinZeroCrossings3, perlinBuckets3) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2.17f, ::perlin)})
        println("3")
        val (perlinZeroes4, perlinZeroCrossings4, perlinBuckets4) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2.23f, ::perlin)})
        println("4")
        val (radialZeroes1, radialZeroCrossings1, radialBuckets1) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2f, ::perlinRadial2v2)})
        println("5")
        val (radialZeroes2, radialZeroCrossings2, radialBuckets2) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2.11f, ::perlinRadial2v2)})
        println("6")
        val (radialZeroes3, radialZeroCrossings3, radialBuckets3) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2.17f, ::perlinRadial2v2)})
        println("7")
        val (radialZeroes4, radialZeroCrossings4, radialBuckets4) = test1DZeroCrossing(defaultPerlinGradients1, p, width, subdivision, {g, p, x -> fractal(g, p, x, 5, 2.23f, ::perlinRadial2v2)})
        println("8")
        for(i in 0 until 20) {
            buffer.append("${perlinZeroCrossings1.buckets[i]},${perlinZeroCrossings2.buckets[i]},${perlinZeroCrossings3.buckets[i]},${perlinZeroCrossings4.buckets[i]},${radialZeroCrossings1.buckets[i]},${radialZeroCrossings2.buckets[i]},${radialZeroCrossings3.buckets[i]},${radialZeroCrossings4.buckets[i]}\n")
        }
        f0.writeText(perlinBuckets1)
        f1.writeText(perlinBuckets2)
        f2.writeText(perlinBuckets3)
        f3.writeText(perlinBuckets4)
        f4.writeText(radialBuckets1)
        f5.writeText(radialBuckets2)
        f6.writeText(radialBuckets3)
        f7.writeText(radialBuckets4)
        f8.writeText(buffer.toString())*/
    } catch (e: Exception) {
        e.printStackTrace()
    }

}