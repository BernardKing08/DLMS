package com.dlms.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequestDto {
    private Long userId;
    private List<Long> bookIds;
    private String pickupLocation;
    private String notes;
}
