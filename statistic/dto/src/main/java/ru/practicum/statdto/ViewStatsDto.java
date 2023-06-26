package ru.practicum.statdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ViewStatsDto {
    private String app;

    private String uri;

    private long hits;
}
