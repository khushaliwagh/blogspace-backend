
package com.example.blogapi.specification;

import com.example.blogapi.entity.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

    public static Specification<Post> titleContains(String title) {
        return (root, query, builder) ->
                title == null || title.isBlank()
                ? null
                : builder.like(
                    builder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
                );
    }

    public static Specification<Post> categoryEquals(String category) {
        return (root, query, builder) ->
                category == null || category.isBlank()
                ? null
                : builder.equal(root.get("category"), category);
    }
}
