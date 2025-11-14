package com.ekino.oss.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.elasticsearch._types.query_dsl.UntypedRangeQuery
import co.elastic.clients.json.JsonData
import com.ekino.oss.metalastic.core.Metamodel
import com.google.common.collect.BoundType
import com.google.common.collect.Range

/** Range query utilities for elasticsearch-java 8.15+ (UntypedRangeQuery API) */
internal fun <T> Metamodel<*>.toRangeQuery(range: Range<out Comparable<T>>) =
  RangeQuery.of {
    it.untyped { rangeQuery ->
      rangeQuery.field(path())
      if (range.hasLowerBound()) {
        val operator = range.toLowerOperator()
        rangeQuery.withBound(range.lowerEndpoint(), operator)
      }
      if (range.hasUpperBound()) {
        val operator = range.toUpperOperator()
        rangeQuery.withBound(range.upperEndpoint(), operator)
      }
      rangeQuery
    }
  }

internal fun Range<*>.toLowerOperator():
  (UntypedRangeQuery.Builder, JsonData) -> UntypedRangeQuery.Builder =
  when (lowerBoundType()) {
    BoundType.CLOSED -> { builder: UntypedRangeQuery.Builder, value: JsonData ->
        builder.apply { gte(value) }
      }
    BoundType.OPEN -> { builder: UntypedRangeQuery.Builder, value: JsonData ->
        builder.apply { gt(value) }
      }
  }

internal fun Range<*>.toUpperOperator():
  (UntypedRangeQuery.Builder, JsonData) -> UntypedRangeQuery.Builder =
  when (upperBoundType()) {
    BoundType.CLOSED -> { builder: UntypedRangeQuery.Builder, value: JsonData ->
        builder.apply { lte(value) }
      }
    BoundType.OPEN -> { builder: UntypedRangeQuery.Builder, value: JsonData ->
        builder.apply { lt(value) }
      }
  }

internal fun <T> UntypedRangeQuery.Builder.withBound(
  value: Comparable<T>,
  operator: (UntypedRangeQuery.Builder, JsonData) -> UntypedRangeQuery.Builder,
) {
  toJsonData(value)?.let { (jsonData, dateFormat) ->
    operator.invoke(this, jsonData)
    dateFormat?.also { format(dateFormat.pattern) }
  }
}
