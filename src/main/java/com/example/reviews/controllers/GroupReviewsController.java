package com.example.reviews.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.example.reviews.models.Group;
import com.example.reviews.models.GroupReview;

@Controller
public class GroupReviewsController {
	
	private final List<GroupReview> reviews = new ArrayList<>();
	
	public GroupReviewsController() {
		Group elide = new Group("com.yahoo.elide");
		Group example = new Group("com.example.repository");
		Group test = new Group("com.test");
		reviews.add(new GroupReview(elide, "1", "Recommended!", 10));
		reviews.add(new GroupReview(elide, "2", "Only ok", 5));
		reviews.add(new GroupReview(example, "3", "Not recommended", 2));
		reviews.add(new GroupReview(example, "4", "Better than expected", 6));
		reviews.add(new GroupReview(test, "5", "Testing", 2));
	}
	
	/**
	 * Group @key(fields: "name") @extends.
	 * 
	 * @param group the group to retrieve reviews for
	 * @return the reviews for a specific group
	 */
	@SchemaMapping
	public List<GroupReview> groupReviews(Group group) {
		return reviews.stream().filter(review -> group.name().equals(review.group().name())).toList();
	}
	
	/**
	 * Query groupReview(id: ID!): GroupReview.
	 * 
	 * @param id the review id
	 * @return the review for group
	 */
	@QueryMapping
	public GroupReview groupReview(@Argument String id) {
		return reviews.stream().filter(review -> review.id().equals(id)).findFirst().orElse(null);
	}
	
	/**
	 * Query groupReviews: [GroupReview!]!.
	 * 
	 * @return the reviews for group
	 */
	@QueryMapping
	public List<GroupReview> groupReviews() {
		return reviews;
	}
}
