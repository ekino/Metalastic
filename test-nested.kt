import com.qelasticsearch.integration.QExampleDocument

fun main() {
    // Test that we can now access the correct nested class
    val document = QExampleDocument
    
    // This should work and access the outer NameCollision class
    val outerNameCollision = document.nameCollision.firstLevel
    println("Outer nameCollision.firstLevel: ${outerNameCollision.path}")
    
    // This should work and access the inner NameCollision class
    val innerNameCollision = document.nestedObject.nameCollision.secondLevel
    println("Inner nestedObject.nameCollision.secondLevel: ${innerNameCollision.path}")
    
    // Verify paths are different
    println("Are paths different? ${outerNameCollision.path != innerNameCollision.path}")
}