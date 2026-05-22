package com.nhan.to_do_api.exception;

import com.nhan.to_do_api.dto.response.ApiResponse;
import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
   @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(AppException exception) {
       ErrorCode errorCode = exception.getErrorCode();
       ApiResponse apiResponse = ApiResponse.builder()
               .code(errorCode.getCode())
               .message(errorCode.getMessage())
               .result(null)
               .build();
       return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
   }
   @ExceptionHandler(AccessDeniedException.class)
   public ResponseEntity<ApiResponse<Void>> handleException(AccessDeniedException exception) {
       ErrorCode errorCode = ErrorCode.FORBIDDEN;
       ApiResponse apiResponse = ApiResponse.builder()
               .code(errorCode.getCode())
               .message(errorCode.getMessage())
               .result(null)
               .build();
       return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
   }
   @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
       ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
       ApiResponse apiResponse = ApiResponse.builder()
               .code(errorCode.getCode())
               .message(errorCode.getMessage())
               .result(null)
               .build();
       return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
   }
   @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(ValidationException exception) {
       ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
       ApiResponse apiResponse = ApiResponse.builder()
               .code(errorCode.getCode())
               .message(errorCode.getMessage())
               .result(null)
               .build();
       return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
   }
   @ExceptionHandler(UnknownError.class)
    public ResponseEntity<ApiResponse<Void>> handleException(UnknownError exception) {
       ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
       ApiResponse apiResponse = ApiResponse.builder()
               .code(errorCode.getCode())
               .message(errorCode.getMessage())
               .result(null)
               .build();
       return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
   }
   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(MethodArgumentNotValidException exception) {
       ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
       ApiResponse apiResponse = ApiResponse.builder()
               .code(errorCode.getCode())
               .message(errorCode.getMessage())
               .result(null)
               .build();
       return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
   }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(HttpMessageNotReadableException exception) {
        ErrorCode errorCode = ErrorCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION;
        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .result(null)
               .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }
}