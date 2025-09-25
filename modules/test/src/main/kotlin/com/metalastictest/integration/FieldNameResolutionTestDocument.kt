package com.metalastictest.integration

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField

@Document(indexName = "field_name_resolution_test")
data class FieldNameResolutionTestDocument(
  @Field(type = FieldType.Keyword) val id: String = "",

  // Case 1: Better naming - should use annotation name for property
  @Field(type = FieldType.Boolean, name = "valid") val isValid: Boolean = false,

  // Case 2: Keep original naming - annotation name doesn't follow conventions better
  @Field(type = FieldType.Text, name = "search_content") val searchableText: String = "",

  // Case 3: Invalid identifier - should keep original property name, use annotation for ES
  @Field(type = FieldType.Text, name = "987987_oi") val userName: String = "",

  // Case 4: MultiField with better naming
  @MultiField(
    mainField = Field(type = FieldType.Text, name = "fulltext"),
    otherFields =
      [
        InnerField(suffix = "keyword", type = FieldType.Keyword),
        InnerField(suffix = "search", type = FieldType.Text),
      ],
  )
  val getText: String = "",

  // Case 5: MultiField keeping original naming
  @MultiField(
    mainField = Field(type = FieldType.Text, name = "description_field"),
    otherFields = [InnerField(suffix = "raw", type = FieldType.Keyword)],
  )
  val description: String = "",

  // Case 6: Object field with custom name
  @Field(type = FieldType.Object, name = "user_profile") val profile: UserProfile = UserProfile(),

  // Case 7: Nested field with custom name
  @Field(type = FieldType.Nested, name = "contact_methods")
  val contacts: List<ContactMethod> = emptyList(),

  // Case 8: No name attribute - should use default behavior
  @Field(type = FieldType.Integer) val age: Int = 0,
) {
  data class UserProfile(
    @Field(type = FieldType.Text, name = "display_name") val name: String = "",
    @Field(type = FieldType.Keyword, name = "email_addr") val email: String = "",
  )

  data class ContactMethod(
    @Field(type = FieldType.Keyword, name = "contact_type") val type: String = "",
    @Field(type = FieldType.Text, name = "contact_value") val value: String = "",
  )
}
