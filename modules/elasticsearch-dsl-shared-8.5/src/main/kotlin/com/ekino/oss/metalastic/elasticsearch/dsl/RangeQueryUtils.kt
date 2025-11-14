package com.ekino.oss.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.json.JsonData
import com.ekino.oss.metalastic.core.Metamodel
import com.google.common.collect.BoundType
import com.google.common.collect.Range

/** Range query utilities for elasticsearch-java 8.5-8.13 (classic RangeQuery API) */
internal fun <T> Metamodel<*>.toRangeQuery(range: Range<out Comparable<T>>) =
  RangeQuery.of { rangeQuery ->
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

internal fun Range<*>.toLowerOperator(): (RangeQuery.Builder, JsonData) -> RangeQuery.Builder =
  when (lowerBoundType()) {
    BoundType.CLOSED -> { builder: RangeQuery.Builder, value: JsonData ->
        builder.apply { gte(value) }
      }
    BoundType.OPEN -> { builder: RangeQuery.Builder, value: JsonData ->
        builder.apply { gt(value) }
      }
  }

internal fun Range<*>.toUpperOperator(): (RangeQuery.Builder, JsonData) -> RangeQuery.Builder =
  when (upperBoundType()) {
    BoundType.CLOSED -> { builder: RangeQuery.Builder, value: JsonData ->
        builder.apply { lte(value) }
      }
    BoundType.OPEN -> { builder: RangeQuery.Builder, value: JsonData ->
        builder.apply { lt(value) }
      }
  }

internal fun <T> RangeQuery.Builder.withBound(
  value: Comparable<T>,
  operator: (RangeQuery.Builder, JsonData) -> RangeQuery.Builder,
) {
  toJsonData(value)?.let { (jsonData, dateFormat) ->
    operator.invoke(this, jsonData)
    dateFormat?.also { format(dateFormat.pattern) }
  }
}
