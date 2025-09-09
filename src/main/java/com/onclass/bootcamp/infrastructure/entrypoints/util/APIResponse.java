package com.onclass.bootcamp.infrastructure.entrypoints.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class APIResponse<T> {
    private String code;
    private String message;
    private String identifier;
    private String date;
    private T data;
    private java.util.List<ErrorDTO> errors;
}
