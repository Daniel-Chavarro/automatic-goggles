# Domain to Entity Mapping Design: Fetch & Update Strategy

## Context
When persisting Domain Models to the database using JPA/Hibernate Entities, several mapping-related bugs can occur:
1. **Duplicate Product Insert / Constraint Violations**: Attempting to insert a duplicate reference when the mapper creates a new entity instance for an existing record.
2. **Orphan References**: Child entities are left orphaned or not properly deleted when replacing collections entirely.
3. **Detached Entity Passed to Persist**: Hibernate throws an exception when an entity with an ID is passed to persist because the mapper instantiated a new entity object instead of using a managed one.

## Proposed Architecture: Fetch & Update Strategy

To solve the friction between pure Domain Models and stateful JPA Entities, the system will adopt the "Fetch & Update" repository strategy.

### 1. Repository Flow (Save Operation)
The interface between the Domain and Persistence layers (the Repository implementation) will intercept the save operation:
- **Creation (No ID)**: If the Domain Model does not have an ID, it is mapped to a completely new Entity. The entity is then persisted using the underlying Spring Data JPA repository.
- **Update (Has ID)**: If the Domain Model has an ID, the Repository will first fetch the existing managed Entity from the database. It will then map the updated fields from the Domain Model onto this managed Entity. Hibernate's dirty checking will handle the update on flush.

### 2. Association Management (Preventing Detached/Duplicate Entities)
When a Domain Model references another aggregate root (e.g., an `Order` containing a `Product`), the mapping process must not instantiate a new `ProductEntity` using the ID.
- **Solution**: The mapper or repository must retrieve a proxy/reference to the existing entity using `EntityManager.getReference()` or `JpaRepository.getReferenceById()`. This links the managed context correctly and prevents Hibernate from trying to persist a detached object or attempting to insert a duplicate record.

### 3. Collection Management (Preventing Orphans)
When updating One-To-Many relationships (e.g., `Order` -> `OrderProduct`), simply replacing the `List` in the Entity with a newly mapped `List` severs the relationship with existing elements, often leading to orphans or constraint errors.
- **Solution**: The mapper must perform a "collection merge" on the existing managed Entity's collection:
  1. Remove elements from the Entity collection that are no longer present in the Domain collection (this triggers `orphanRemoval = true`).
  2. Update scalar fields of elements that exist in both collections.
  3. Add completely new elements from the Domain collection to the Entity collection.

## Implementation Guidelines
- **Mappers**: Avoid using pure MapStruct `Domain -> Entity` for updates without a `@MappingTarget`. Always use `@MappingTarget` to map Domain updates onto the fetched existing Entity.
- **Entity Configurations**: Ensure One-To-Many relationships have `cascade = CascadeType.ALL` and `orphanRemoval = true`.
- **References**: Ensure the mapping layer has access to `EntityManager` or related Repositories to resolve references via ID.
