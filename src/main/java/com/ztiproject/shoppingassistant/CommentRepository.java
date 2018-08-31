package com.ztiproject.shoppingassistant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findByPost(PostId id);

}
