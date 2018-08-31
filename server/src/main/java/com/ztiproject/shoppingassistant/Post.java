package com.ztiproject.shoppingassistant;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import static com.ztiproject.shoppingassistant.Post.Status.DRAFT;

@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post implements Serializable {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @Builder.Default
    private Status status = DRAFT;

    @CreatedDate
    private LocalDateTime createdDate;

    @CreatedBy
    private Username author;

    enum Status {
        DRAFT,
        PUBLISHED
    }
}
