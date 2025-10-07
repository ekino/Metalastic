package com.metalastic.elasticsearch.dsl

import com.google.common.collect.BoundType
import com.google.common.collect.Range

@JvmInline
value class StartBound<T : Comparable<T>>(val range: Range<T>) {
  operator fun rangeTo(to: T?): Range<T>? =
    when {
      to == null ->
        if (range.hasLowerBound()) {
          range
        } else {
          null
        }
      !range.hasLowerBound() -> Range.atMost(to)
      else ->
        if (range.lowerEndpoint() > to) {
          Range.all()
        } else {
          when (range.lowerBoundType()) {
            BoundType.OPEN -> Range.openClosed(range.lowerEndpoint(), to)
            BoundType.CLOSED -> Range.closed(range.lowerEndpoint(), to)
          }
        }
    }

  operator fun rangeUntil(to: T?): Range<T>? =
    when {
      to == null ->
        if (range.hasLowerBound()) {
          range
        } else {
          null
        }
      !range.hasLowerBound() -> Range.lessThan(to)
      else ->
        if (range.lowerEndpoint() > to) {
          Range.all()
        } else {
          when (range.lowerBoundType()) {
            BoundType.OPEN -> Range.open(range.lowerEndpoint(), to)
            BoundType.CLOSED -> Range.closedOpen(range.lowerEndpoint(), to)
          }
        }
    }
}

/**
 * Creates a range bound type where
 * * lower bound is closed (inclusive) `[`
 * * upper bound depends on the operator used (`..` for closed, `..<` for open)
 */
@VariantDsl
fun <T : Comparable<T>> T?.fromInclusive() =
  when (this) {
    null -> StartBound(Range.all())
    else -> StartBound(Range.atLeast(this))
  }

/**
 * Creates a range bound type where
 * * lower bound is open (exclusive) `(`
 * * upper bound depends on the operator used (`..` for closed, `..<` for open)
 */
@VariantDsl
fun <T : Comparable<T>> T?.fromExclusive() =
  when (this) {
    null -> StartBound(Range.all())
    else -> StartBound(Range.greaterThan(this))
  }
