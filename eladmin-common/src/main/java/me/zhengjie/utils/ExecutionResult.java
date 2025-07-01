package me.zhengjie.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A standardized response object for service operations.
 * Used to return operation results with an ID and optional additional data.
 */
public record ExecutionResult(Long id, Object data) {
    
    /**
     * Create a result with just an ID
     * 
     * @param id The entity ID
     * @return ExecutionResult with the ID and null data
     */
    public static ExecutionResult of(Long id) {
        return new ExecutionResult(id, null);
    }
    
    /**
     * Create a result with an ID and data
     * 
     * @param id The entity ID
     * @param data Additional data related to the operation
     * @return ExecutionResult with the ID and data
     */
    public static ExecutionResult of(Long id, Object data) {
        return new ExecutionResult(id, data);
    }
    
    /**
     * Create a result for deletion operations
     * 
     * @param id The ID of the deleted entity
     * @return ExecutionResult with the ID and a map containing deleted=true
     */
    public static ExecutionResult ofDeleted(Long id) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("deleted", true);
        return new ExecutionResult(id, result);
    }
    
    /**
     * Convert the ExecutionResult to a Map for response bodies
     * 
     * @return A map containing the ID and data (if present)
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        if (data != null) {
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                result.putAll(dataMap);
            } else {
                result.put("data", data);
            }
        }
        return result;
    }
}
