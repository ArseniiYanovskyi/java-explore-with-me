package ru.practicum.comment.model.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class CommentReplyDto {
    private Long id;
    private Long commentatorId;
    private Long commentId;
    @NotNull
    @NotBlank
    @Length(min = 1, max = 7000)
    private String text;
    private String timestamp;
    private Boolean edited;
    private String editedTime;
}
