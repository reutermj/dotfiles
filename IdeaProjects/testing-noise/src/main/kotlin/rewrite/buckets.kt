package rewrite

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