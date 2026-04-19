package com.deliacte.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyStatsDto {

    private Integer year;
    private Integer month;
    private Long totalCreated;
    private Long totalCompleted;
}
