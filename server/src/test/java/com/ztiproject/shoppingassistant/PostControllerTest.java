package com.ztiproject.shoppingassistant;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = PostController.class)
public class PostControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    PostRepository posts;

    @MockBean
    CommentRepository comments;

    @Test
    public void getAllPosts_shouldBeOk() {
        given(posts.findAll())
            .willReturn(Flux.just(Post.builder().id("1").title("my first post").content("content of my first post").build()));

        client.get().uri("/posts").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].title").isEqualTo("my first post")
            .jsonPath("$[0].id").isEqualTo("1")
            .jsonPath("$[0].content").isEqualTo("content of my first post");

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);

    }

    @Test
    public void getAllPostsByKeyword_shouldBeOk() {
        List<Post> data = IntStream.range(1, 16)//15 posts will be created.
            .mapToObj(n -> Post.builder()
                .id("" + n)
                .title("my " + n + " first post")
                .content("content of my " + n + " first post")
                .createdDate(LocalDateTime.now())
                .build())
            .collect(toList());

        given(posts.findAll())
            .willReturn(Flux.fromIterable(data));

        client.get().uri("/posts").exchange()
            .expectStatus().isOk()
            .expectBodyList(Post.class).hasSize(10);
        client.get().uri("/posts?page={page}", 1).exchange()
            .expectStatus().isOk()
            .expectBodyList(Post.class).hasSize(5);

        client.get().uri("/posts/count").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.count").isEqualTo(15);

        client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/posts/count")
                .queryParam("q", "5")
                .build()
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.count").isEqualTo(2);

        verify(this.posts, times(4)).findAll();
        verifyNoMoreInteractions(this.posts);

    }

    @Test
    public void getPostById_shouldBeOk() {
        given(posts.findById("1"))
            .willReturn(Mono.just(Post.builder().id("1").title("my first post").content("content of my first post").build()));

        client.get().uri("/posts/1").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("my first post")
            .jsonPath("$.id").isEqualTo("1")
            .jsonPath("$.content").isEqualTo("content of my first post");

        verify(this.posts, times(1)).findById(anyString());
        verifyNoMoreInteractions(this.posts);

    }

    @Test
    @Ignore
    public void getPostByNonExistedId_shouldReturn404() {
        given(posts.findById("1"))
            .willReturn(Mono.empty());

        client.get().uri("/posts/1").exchange()
            .expectStatus().isNotFound();

        verify(this.posts, times(1)).findById(anyString());
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void updatePost_shouldBeOk() {
        Post post = Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build();

        given(posts.findById("1"))
            .willReturn(Mono.just(post));

        post.setTitle("updated title");
        post.setContent("updated content");

        given(posts.save(post))
            .willReturn(Mono.just(Post.builder().id("1").title("updated title").content("updated content").createdDate(LocalDateTime.now()).build()));

        client.put().uri("/posts/1").body(BodyInserters.fromObject(post))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("updated title")
            .jsonPath("$.id").isEqualTo("1")
            .jsonPath("$.content").isEqualTo("updated content")
            .jsonPath("$.createdDate").isNotEmpty();

        verify(this.posts, times(1)).findById(anyString());
        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void updatePostStatus_shouldBeOk() {
        Post post = Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build();

        given(posts.findById("1"))
            .willReturn(Mono.just(post));

        post.setStatus(Post.Status.PUBLISHED);

        given(posts.save(post))
            .willReturn(Mono.just(Post.builder().id("1").title("updated title").content("updated content").createdDate(LocalDateTime.now()).build()));

        client.put().uri("/posts/1/status").body(BodyInserters.fromObject(new StatusUpdateRequest("PUBLISHED")))
            .exchange()
            .expectStatus().isNoContent();

        verify(this.posts, times(1)).findById(anyString());
        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void createPost_shouldBeOk() {
        Post post = Post.builder().title("my first post").content("content of my first post").build();
        given(posts.save(post))
            .willReturn(Mono.just(Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build()));

        client.post().uri("/posts").body(BodyInserters.fromObject(post))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("my first post")
            .jsonPath("$.id").isEqualTo("1")
            .jsonPath("$.content").isEqualTo("content of my first post")
            .jsonPath("$.createdDate").isNotEmpty();

        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void deletePost_shouldBeOk() {
        Post post = Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build();

        given(posts.findById("1"))
            .willReturn(Mono.just(post));
        Mono<Void> mono = Mono.empty();
        given(posts.delete(post))
            .willReturn(mono);

        client.delete().uri("/posts/1")
            .exchange()
            .expectStatus().isNoContent();

        verify(this.posts, times(1)).findById(anyString());
        verify(this.posts, times(1)).delete(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void getCommentsByPostId_shouldBeOk() {
        given(comments.findByPost( new PostId("1")))
            .willReturn(Flux.just(Comment.builder().id("comment-id-1").post(new PostId("1")).content("comment of my first post").build()));

        client.get().uri("/posts/1/comments").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo("comment-id-1")
            .jsonPath("$[0].content").isEqualTo("comment of my first post");

        verify(this.comments, times(1)).findByPost(any(PostId.class));
        verifyNoMoreInteractions(this.comments);

    }

    @Test
    public void createCommentOfPost_shouldBeOk() {

        given(comments.save(Comment.builder().content("comment of my first post").build()))
            .willReturn(Mono.just(Comment.builder().id("comment-id-1").post(new PostId("1")).content("content of my first post").createdDate(LocalDateTime.now()).build()));

        CommentForm form = CommentForm.builder().content("comment of my first post").build();
        client.post().uri("/posts/1/comments").body(BodyInserters.fromObject(form))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo("comment-id-1")
            .jsonPath("$.content").isEqualTo("content of my first post")
            .jsonPath("$.createdDate").isNotEmpty();

        verify(this.comments, times(1)).save(any(Comment.class));
        verifyNoMoreInteractions(this.comments);
    }

}
