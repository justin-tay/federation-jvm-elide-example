package com.example.reviews.models;

public record GroupReview(Group group, String id, String text, Integer stars) {
	public GroupReview(Group group, String id, Integer stars) {
		this(group, id, null, stars);
	}
}
