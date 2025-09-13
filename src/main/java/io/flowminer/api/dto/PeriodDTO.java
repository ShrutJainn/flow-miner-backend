package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PeriodDTO {
    private int month;
    private int year;
}
