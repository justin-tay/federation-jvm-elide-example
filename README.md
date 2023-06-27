# Federation JVM Elide Example

This example demonstrates GraphQL Federation with Elide.

This uses Spring GraphQL to contribute a subgraph that integrates with the subgraph contributed by Elide.

## Demo
1\. Ensure that the Elide Spring Boot Example has GraphQL Federation enabled

```yaml
elide:
  graphql:
    federation:
      enabled: true
```

2\. Run the Elide Spring Boot Example

```bash
mvn spring-boot:run
```

Graphiql can be accessed at http://localhost:8080/graphiql/index.html.

3\. Run the Federation JVM Elide Example

```bash
mvn spring-boot:run
```

Graphiql can be accessed at http://localhost:8081/graphiql?path=/graphql.

4\. Ensure Rover CLI is installed

Rover is the command-line interface for managing and maintaining graphs with Apollo GraphOS.

```bash
npm install -g @apollo/rover
```

5\. Start Rover CLI in separate consoles

Elide Spring Boot Example

```bash
rover dev --name groups --url http://localhost:8080/graphql/api
```

Federation JVM Elide Example

```bash
rover dev --name reviews --schema ./src/main/resources/graphql/schema.graphqls --url http://localhost:8081/graphql
```

6\. Query Editor
Access http://localhost:4000 for the query editor.

## Queries

### Entity Query on JVM Elide Example

The Federation JVM Elide Example extends the `Group` entity on the Elide Spring Boot Example with the `groupReviews` field.

This requires the `Group` on Federation JVM Elide Example to have the `@key` directive.

```
type Group @key(fields: "name") @extends {
    name: DeferredID! @external
    groupReviews: [GroupReview!]!
}
```

The following query is an example that starts from the `Group` entity on Elide Spring Boot Example.

```
query {
  group {
    edges {
      node {
        commonName
        groupReviews {
          stars
          text
        }
      }
    }
  }
}
```

The router calls Federation JVM Elide Example with the following query to add the additional fields to the `Group` entity.

```
query {
  _entities(representations: [{__typename: "Group", name: "com.yahoo.elide"}]) {
    ... on Group {
      stars
      text
    }
  }
}
```

The mapping for the representations to the Group is configured in the entity data fetcher in `com.example.reviews.config.GraphQLConfiguration`.

```java
DataFetcher<?> entityDataFetcher = env -> {
  List<Map<String, Object>> representations = env.getArgument(_Entity.argumentName);
  return representations.stream().map(representation -> {
    // Assume only a single id key and no composite keys
    String idKey = representation.keySet().stream().filter(key -> !"__typename".equals(key)).findFirst()
        .orElse(null);
    String id = (String) representation.get(idKey);
    if (GROUP_TYPE.equals(representation.get("__typename"))) {
      return new Group(id);
    }
    return null;
  }).toList();
};
```

### Entity Query on Elide Spring Boot Example

The Federation JVM Elide Example contains the `Group` entity from Elide Spring Boot Example on the `GroupReview` entity.

This requires the `Group` on Elide Spring Boot Example to have the `@key` directive.

The following is the schema of `Group` on Elide Spring Boot Example.

```
type Group @key(fields : "name") {
  commonName: String
  description: String
  name: DeferredID
  products(after: String, data: [ProductInput], filter: String, first: String, ids: [String], op: ElideRelationshipOp = FETCH, sort: String): ProductConnection
}
```

The following is the schema of `GroupReview` on Federation JVM Elide Example.

```
type GroupReview {
    id: ID!,
    text: String
    stars: Int!
    group: Group
}
```

The following query is an example that starts from the `GroupReview` entity on Federation JVM Elide Example and references the `Group` on Elide Spring Boot Example.

```
query {
  groupReviews {
    id
    stars
    text
    group {
      name
      commonName
    }
  }
}
```

The router calls Elide Spring Boot Example with the following query.

```
query {
  _entities(representations: [{__typename: "Group", name: "com.yahoo.elide"}]) {
    ... on Group {
      name
      commonName
    }
  }
}
```

Elide will determine the projection in `GraphQLEntityProjectionMaker`.

The `EntitiesDataFetcher` will fetch a list of `NodeContainer`.

```java
public class EntitiesDataFetcher implements DataFetcher<List<NodeContainer>> {
  ...
}
```

The `EntityTypeResolver` will map the `NodeContainer` to the appropriate `GraphQLObjectType`.


## Schema

Elide uses a custom scalar `DeferredID` instead of `ID`.

This needs to be registered with the Federation JVM Elide Example subgraph.

The following is the schema definition.

```
scalar DeferredID
```

The following is the Java code for the `GraphQLScalarType`.

```java
public class GraphQLScalars {
  public static GraphQLScalarType DEFERRED_ID = GraphQLScalarType.newScalar().name("DeferredID")
      .description("The DeferredID scalar type represents a unique identifier.")
      .coercing(new Coercing<Object, String>() {
        @Override
        public String serialize(Object o) {
          return o.toString();
        }

        @Override
        public String parseValue(Object o) {
          return o.toString();
        }

        @Override
        public String parseLiteral(Object o) {
          if (o instanceof StringValue stringValue) {
            return stringValue.getValue();
          }
          if (o instanceof IntValue intValue) {
            return intValue.getValue().toString();
          }
          return o.toString();
        }
      }).build();
}
```

The following is the Java code for registering the scalar in `GraphQLConfiguration`.

```java
@Bean
public GraphQlSourceBuilderCustomizer graphqlSourceBuilderCustomizer() {
  return schemaResourceBuilder -> schemaResourceBuilder
    .configureRuntimeWiring(runtimeWiring -> runtimeWiring.scalar(GraphQLScalars.DEFERRED_ID));
}
```