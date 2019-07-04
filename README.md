## JPA Nested Set

This is an implementation of the [Nested Set model](https://en.wikipedia.org/wiki/Nested_set_model)
for storing trees in relational databases with Java and [JPA 2.x](https://en.wikipedia.org/wiki/Java_Persistence_API#JPA_2.0).

It uses the variant with [persistent depths](https://en.wikipedia.org/wiki/Nested_set_model#Variations)
attached to each node and supports multiple trees in a single table, including moving
branches across trees.

The nested set model can be suitable for deeply nested tree structures which require
frequent and diverse retrieval of arbitrary branches. It is not suited to trees that
are comparatively frequently updated, since updates often require renumbering large
parts of a tree. Also, it might not be the best option if your database supports efficient
"recursive" queries or other dedicated features for working with hierarchical structures.

### Repository Status

This repository is not currently actively maintained. Forks are welcome.
If you would like to become an active collaborator on this repository, or you have
an actively maintained fork that you want me to link to from here, please
contact me (e.g. via opening an issue).

At the time of this writing, the most recently updated fork seems to
reside at [vitapublic](https://github.com/vitapublic/JPA-NestedSet).

### Basic Usage

Using this library generally involves the following steps:

  1. Implement the `NodeInfo` interface for the JPA entity that is to
     represent a node in a tree, annotating the fields for
     storing the `left`, `right`, `level` and `root` values with
     the annotations `@LeftColumn`, `@RightColumn`, `@LevelColumn`
     and `@RootColumn`, respectively.
     For an example see the
     [Category](src/test/java/org/pkaboo/jpa/nestedset/model/Category.java)
     entity from the test suite.
     Create the matching database schema accordingly.

  2. Whenever you want to operate on the tree structure of
     the entities defined in step 1, create a `JpaNestedSetManager`,
     passing to it an `EntityManager` in whose context all JPA
     operations are performed. That is, a `JpaNestedSetManager`
     always operates within the scope of a specific `EntityManager`
     and hence must not outlive it.

  3. Use the `JpaNesteSetManager` to operate on the tree, e.g.
     create new root nodes, lookup existing nodes, move
     nodes around the tree(s) etc, as provided by the `Node`
     and `NestedSetManager` interfaces.
     For an example, see the code in the
     [BasicTest](src/test/java/org/pkaboo/jpa/nestedset/BasicTest.java)
     and
     [FunctionalTest](src/test/java/org/pkaboo/jpa/nestedset/FunctionalNestedSetTest.java)
     files from the test suite.

### Concurrency & Tree Integrity

The current implementation does not in itself maintain integrity of tree structures
(left / right / level / root attributes) in the presence of concurrent modifications.
If there is a possibility for concurrent tree modifications, measures must be
taken to prevent tree corruption, e.g. through SERIALIZABLE transaction isolation
or additional [optimistic](http://martinfowler.com/eaaCatalog/optimisticOfflineLock.html)
or [pessimistic](http://martinfowler.com/eaaCatalog/pessimisticOfflineLock.html)
locks.

