package ru.practicum.comment.model.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class CommentDto {
    private Long id;
    private Long commentatorId;
    private Long eventId;
    @NotNull
    @NotBlank
    @Length(min = 1, max = 7000)
    private String text;
    private String timestamp;
    private List<CommentReplyDto> replies;
    private Boolean edited;
    private String editedTime;
}
