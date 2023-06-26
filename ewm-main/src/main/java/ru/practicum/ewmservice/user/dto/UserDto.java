package ru.practicum.ewmservice.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewmservice.user.annotation.EmailMaxLengthValid;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

    @NotBlank
    @Email
    @EmailMaxLengthValid
    @Size(min = 6, max = 254)
    private String email;
}
