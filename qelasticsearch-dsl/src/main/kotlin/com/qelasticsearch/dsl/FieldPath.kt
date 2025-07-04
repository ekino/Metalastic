package com.qelasticsearch.dsl

/**
 * Represents a field path with information about nested structure.
 * This allows for more intelligent query building where nested paths
 * require special handling (e.g., nested queries in Elasticsearch).
 */
data class FieldPath(
    /**
     * The full dotted path (e.g., "address.city", "activities.name")
     */
    val path: String,
    
    /**
     * List of nested segments in the path.
     * Each segment represents a part of the path that is nested.
     * 
     * Examples:
     * - "address.city" where address is object → nestedSegments = []
     * - "activities.name" where activities is nested → nestedSegments = ["activities"]
     * - "user.addresses.city" where addresses is nested → nestedSegments = ["user.addresses"]
     */
    val nestedSegments: List<String> = emptyList()
) {
    
    /**
     * Returns true if this path contains any nested segments
     */
    val isNested: Boolean
        get() = nestedSegments.isNotEmpty()
    
    /**
     * Returns true if this entire path is within a nested structure
     */
    val isCompletelyNested: Boolean
        get() = nestedSegments.isNotEmpty() && nestedSegments.any { path.startsWith(it) }
    
    /**
     * Gets the root nested path if this field is within a nested structure
     */
    val rootNestedPath: String?
        get() = nestedSegments.find { path.startsWith(it) }
    
    /**
     * Creates a child path from this path
     */
    fun child(childName: String, isChildNested: Boolean = false): FieldPath {
        val newPath = if (path.isEmpty()) childName else "$path.$childName"
        val newNestedSegments = if (isChildNested) {
            nestedSegments + newPath
        } else {
            nestedSegments
        }
        return FieldPath(newPath, newNestedSegments)
    }
    
    /**
     * Returns the string representation for backward compatibility
     */
    override fun toString(): String = path
    
    companion object {
        /**
         * Creates a root path (empty path)
         */
        fun root(): FieldPath = FieldPath("")
        
        /**
         * Creates a simple path without nested information
         */
        fun simple(path: String): FieldPath = FieldPath(path)
        
        /**
         * Creates a nested path
         */
        fun nested(path: String): FieldPath = FieldPath(path, listOf(path))
    }
}