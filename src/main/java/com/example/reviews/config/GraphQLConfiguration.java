package com.example.reviews.config;

import static com.example.reviews.models.Group.GROUP_TYPE;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.ClassNameTypeResolver;

import com.apollographql.federation.graphqljava.Federation;
import com.apollographql.federation.graphqljava._Entity;
import com.example.reviews.models.GraphQLScalars;
import com.example.reviews.models.Group;

import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;

@Configuration
public class GraphQLConfiguration {

	@Bean
	public GraphQlSourceBuilderCustomizer graphqlSourceBuilderCustomizer() {
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
		TypeResolver entityTypeResolver = new ClassNameTypeResolver();
		return builder -> builder
				.configureRuntimeWiring(runtimeWiring -> runtimeWiring.scalar(GraphQLScalars.DEFERRED_ID))
				.schemaFactory((registry, wiring) -> {
					return Federation.transform(registry, wiring).fetchEntities(entityDataFetcher)
							.resolveEntityType(entityTypeResolver).build();
				});
	}
}