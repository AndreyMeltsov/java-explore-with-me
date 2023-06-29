package ru.practicum.statdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class HitDto {
    @NotBlank
    @Size(max = 100)
    private String app;

    @NotBlank
    @URL
    private String uri;

    @NotBlank
    @Size(max = 25)
    private String ip;

    private String timestamp;
}
