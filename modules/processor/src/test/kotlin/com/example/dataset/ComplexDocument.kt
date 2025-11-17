/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.example.dataset

import java.util.Date
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "complex")
data class ComplexDocument(
  @Field(type = FieldType.Keyword) val id: String,
  @Field(type = FieldType.Object) val person: Person,
  @Field(type = FieldType.Nested) val orders: List<Order>,
) {
  data class Person(
    @Field(type = FieldType.Text) val name: String,
    @Field(type = FieldType.Object) val contact: Contact,
  ) {
    data class Contact(
      @Field(type = FieldType.Keyword) val email: String,
      @Field(type = FieldType.Object) val address: Address,
    ) {
      data class Address(
        @Field(type = FieldType.Text) val street: String,
        @Field(type = FieldType.Keyword) val city: String,
      )
    }
  }

  data class Order(
    @Field(type = FieldType.Keyword) val orderId: String,
    @Field(type = FieldType.Date) val orderDate: Date,
    @Field(type = FieldType.Nested) val items: List<OrderItem>,
  ) {
    data class OrderItem(
      @Field(type = FieldType.Keyword) val productId: String,
      @Field(type = FieldType.Integer) val quantity: Int,
    )
  }
}
