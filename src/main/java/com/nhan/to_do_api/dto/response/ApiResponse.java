package com.nhan.to_do_api.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.config.TypeFilterParser;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE )
public class ApiResponse<T> {
    @Builder.Default
   int code = 1000;
   T result;
   String message;
}