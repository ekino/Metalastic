package com.metalastictest.integration

import com.metalastic.integration.QIndexPerson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.reflect.typeOf

class CompanionObjectTest :
  ShouldSpec({
    should("provide static access via companion object") {
      // Test that companion object provides static access
      val staticInstance = QIndexPerson.indexPerson
      val manualInstance = QIndexPerson<Any>(fieldType = typeOf<Any>())

      // Both should be valid instances
      staticInstance shouldNotBe null
      manualInstance shouldNotBe null

      // Static instance should have the same structure as manual instance
      staticInstance.firstName.path() shouldBe manualInstance.firstName.path()
      staticInstance.lastName.path() shouldBe manualInstance.lastName.path()
      staticInstance.age.path() shouldBe manualInstance.age.path()
    }

    should("work with nested object access") {
      // Test nested object access via static instance
      val staticPerson = QIndexPerson.indexPerson

      staticPerson.address.city.path() shouldBe "address.city"
      staticPerson.address.street.path() shouldBe "address.street"
      staticPerson.activities.name.path() shouldBe "activities.name"
    }

    should("work with different document types") {
      // Test with another document type
      val staticTestDoc = QTestDocument.testDocument

      staticTestDoc.name.path() shouldBe "name"
      staticTestDoc.address.city.path() shouldBe "address.city"
      staticTestDoc.tags.name.path() shouldBe "tags.name"
    }

    should("maintain proper Java compatibility") {
      // Since we use @JvmField, this should work from Java as well
      // The companion object property should be accessible as a static field
      val companionField = QIndexPerson::class.java.getField("indexPerson")
      companionField shouldNotBe null

      val staticValue = companionField.get(null) as QIndexPerson<*>
      staticValue shouldNotBe null
      staticValue.firstName.path() shouldBe "firstName"
    }
  })
