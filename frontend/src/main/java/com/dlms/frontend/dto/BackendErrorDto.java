package com.dlms.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors just the field we care about from each domain service's
 * ErrorResponseDto - the human-readable message - so a 400 from e.g. Auth
 * (bad credentials, validation failure) can be shown back to the user
 * instead of falling through to a generic error page.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackendErrorDto {
    private String errorMsg;
}
