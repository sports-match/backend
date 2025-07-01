# ExecutionResult Pattern

## Overview

The `ExecutionResult` class has been introduced to standardize the return types of service methods that perform write operations (create, update, delete). This replaces the previous approach of using void return types or inconsistent return objects.

## Class Definition

```java
public record ExecutionResult(Long id, Object data) {
    
    // Create a result with just an ID
    public static ExecutionResult of(Long id) { ... }
    
    // Create a result with an ID and data
    public static ExecutionResult of(Long id, Object data) { ... }
    
    // Create a result for deletion operations
    public static ExecutionResult ofDeleted(Long id) { ... }
    
    // Convert the ExecutionResult to a Map for response bodies
    public Map<String, Object> toMap() { ... }
}
```

## How to Use

### 1. Update Service Interfaces

Change void return types to ExecutionResult:

```java
// Before
void create(Entity resources);

// After
ExecutionResult create(Entity resources);
```

### 2. Update Service Implementations

Return appropriate ExecutionResult objects:

```java
// Before
public void create(Entity resources) {
    entityRepository.save(resources);
}

// After
public ExecutionResult create(Entity resources) {
    Entity saved = entityRepository.save(resources);
    return ExecutionResult.of(saved.getId());
}
```

For delete operations:

```java
// Before
public void deleteAll(Long[] ids) {
    for (Long id : ids) {
        entityRepository.deleteById(id);
    }
}

// After
public ExecutionResult deleteAll(Long[] ids) {
    for (Long id : ids) {
        entityRepository.deleteById(id);
    }
    return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
}
```

### 3. Update Controllers

Use the toMap() method to convert ExecutionResult to response bodies:

```java
// Before
public ResponseEntity<Object> create(@Validated @RequestBody Entity resources) {
    entityService.create(resources);
    return new ResponseEntity<>(HttpStatus.CREATED);
}

// After
public ResponseEntity<Object> create(@Validated @RequestBody Entity resources) {
    ExecutionResult result = entityService.create(resources);
    return new ResponseEntity<>(result.toMap(), HttpStatus.CREATED);
}
```

## Benefits

1. **Consistency**: All write operations now return the same type with a predictable structure
2. **Information**: Clients always receive the ID of the affected entity and any relevant operation data
3. **Flexibility**: Additional operation metadata can be included in the data field when needed
4. **Standardization**: Response format is consistent across the entire API

## Implementation Status

- ✅ ExecutionResult class created in common module
- ✅ PlayerService updated to use ExecutionResult
- ✅ SportService updated to use ExecutionResult
- ⬜ Remaining services need to be updated

## Implementation Checklist

- [ ] CourtService
- [ ] ClubService
- [ ] EventService
- [ ] TeamService
- [ ] TeamPlayerService
- [ ] MatchService
- [ ] MatchGroupService
- [ ] QuestionService
- [ ] PlayerAnswerService
- [ ] WaitListService
