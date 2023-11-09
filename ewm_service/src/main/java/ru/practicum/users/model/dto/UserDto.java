package ru.practicum.users.model.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    private long id;
    @NotNull
    @NotBlank
    @Email
    @Length(min = 6, max = 254)
    private String email;
    @NotNull
    @NotBlank
    @Length(min = 2, max = 250)
    private String name;
}
