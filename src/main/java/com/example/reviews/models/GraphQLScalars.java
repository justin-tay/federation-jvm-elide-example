
package com.example.reviews.models;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

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
