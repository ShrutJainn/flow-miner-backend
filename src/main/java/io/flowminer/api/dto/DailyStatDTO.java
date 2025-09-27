package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyStatDTO {
    private int success;
    private int failed;

    public void incrementSuccess() {
        this.success++;
    }
    public void incrementFailed() {
        this.failed++;
    }
}
