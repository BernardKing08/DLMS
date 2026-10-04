package com.dlms.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BorrowRecordDto {

    private Long id;
    private Long userId;
    private Long bookId;
    private String pickupLocation;
    private String notes;
    private String status;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;

    // View-helper fields populated by frontend
    private String bookTitle;
    private String bookAuthor;
}
