/**
 * Copyright (c) 2020, Saalfeld Lab, HHMI Janelia
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.imglib2.imklib

import net.imglib2.*
import net.imglib2.realtransform.AffineGet
import net.imglib2.util.Intervals

val LongArray.interval get() = FinalInterval(*this)
val Collection<Long>.interval get() = toLongArray().interval
val IntArray.interval get() = map { it.toLong() }.interval

val LongArray.intervalMinMax get() = Intervals.createMinMax(*this)
val Collection<Long>.intervalMinMax get() = toLongArray().intervalMinMax
val IntArray.intervalMinMax get() = map { it.toLong() }.intervalMinMax

val DoubleArray.interval get() = FinalRealInterval(DoubleArray(size) { 0.0 }, this)
val Collection<Double>.interval get() = toDoubleArray().interval
val FloatArray.interval get() = map { it.toDouble() }.interval

val DoubleArray.intervalMinMax get() = Intervals.createMinMaxReal(*this)
val Collection<Double>.intervalMinMax get() = toDoubleArray().intervalMinMax
val FloatArray.intervalMinMax get() = map { it.toDouble() }.intervalMinMax

fun Interval.translate(vararg translation: Long) = Intervals.translate(this, *translation)
fun Interval.translate(translation: Localizable) = translate(*translation.positionAsLongArray())
fun Interval.translateInverse(vararg translation: Long) = Intervals.translateInverse(this, *translation)
fun Interval.translateInverse(translation: Localizable) = translateInverse(*translation.positionAsLongArray())
operator fun Interval.plus(translation: LongArray) = translate(*translation)
operator fun Interval.plus(translation: Localizable) = translate(translation)
operator fun Interval.minus(translation: LongArray) = translateInverse(*translation)
operator fun Interval.minus(translation: Localizable) = translateInverse(translation)

val Interval.minAsLongs: LongArray get() = minAsLongArray()
val Interval.maxAsLongs: LongArray get() = maxAsLongArray()
val Interval.minAsInts: IntArray get() = IntArray(numDimensions()) { min(it).toInt() }
val Interval.maxAsInts: IntArray get() = IntArray(numDimensions()) { max(it).toInt() }
val Interval.minAsPoint: Point get() = minAsPoint()
val Interval.maxAsPoint: Point get() = maxAsPoint()

val Interval.numElements: Long get() = Intervals.numElements(this)

infix fun Interval.intersect(that: Interval) = Intervals.intersect(this, that)
infix fun Interval.union(that: Interval) = Intervals.union(this, that)
operator fun Interval.contains(that: Interval) = Intervals.contains(this, that)
operator fun Interval.contains(location: Localizable) = Intervals.contains(this, location)
operator fun Interval.contains(location: IntArray) = containsAll(*location)
operator fun Interval.contains(location: LongArray) = containsAll(*location)
fun Interval.containsAll(vararg location: Int): Boolean {
    for (d in 0 until numDimensions()) {
        val p = location[d]
        if (p < min(d) || p > max(d))
            return false
    }
    return true
}
fun Interval.containsAll(vararg location: Long): Boolean {
    for (d in 0 until numDimensions()) {
        val p = location[d]
        if (p < min(d) || p > max(d))
            return false
    }
    return true
}

fun RealInterval.transformBoundingBox(transformation: AffineGet): RealInterval {
    val min = DoubleArray(nDim) { Double.POSITIVE_INFINITY }
    val max = DoubleArray(nDim) { Double.NEGATIVE_INFINITY }
    val pos = minAsDoubleArray()
    val posTransform = minAsDoubleArray()
    var d = 0
    while (d < nDim) {
        transformation.apply(pos, posTransform)
        println("${pos.map { it }} ${posTransform.map{ it }}")
        (0 until nDim).forEach { min[it] = kotlin.math.min(posTransform[it], min[it]) }
        (0 until nDim).forEach { max[it] = kotlin.math.max(posTransform[it], max[it]) }
        d = 0
        while (d < nDim) {
            pos[d] = pos[d] + realMax(d) - realMin(d)
            if (pos[d] <= realMax(d))
                break
            else
                pos[d] = realMin(d)
            ++d
        }
    }
    return FinalRealInterval(min, max)
}

val RealInterval.smallestsContaining get() = Intervals.smallestContainingInterval(this)

val Interval.isEmpty: Boolean get() = Intervals.isEmpty(this)