package com.nhan.to_do_api.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    SUCCESS(1000, "Success", HttpStatus.OK),
    //request
    INVALID_REQUEST(4000, "Invalid request", HttpStatus.BAD_REQUEST),
    //user
    USER_NOT_FOUND(4001, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(4002, "Username already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(4003, "Email already exists", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME_OR_PASSWORD(4004, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    USER_DISABLED(7005, "User disabled", HttpStatus.FORBIDDEN), //Add Nex ErrorCode
    //author
    UNAUTHORIZED(4005, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(4006, "Forbidden", HttpStatus.FORBIDDEN),
    //todo
    TODO_NOT_FOUND(4007, "Todo not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(4008, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_ALREADY_EXISTS(4009, "Category name already exists", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_CATEGORY_HAS_TODOS(4010, "Cannot delete category because it still has todos", HttpStatus.BAD_REQUEST),
    //todo
    INVALID_TODO_STATUS(4011, "Invalid todo status", HttpStatus.BAD_REQUEST),
    INVALID_DUE_DATE(4012, "Invalid due date", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_INCORRECT(4013, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    //ENUM
    HTTP_MESSAGE_NOT_READABLE_EXCEPTION(4014,"Enum is Wrong " , HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_CANNOT_CREATE(5001,"Toekn Cannot Create",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_AUTHORIZATION_HEADER(401,"AuthorizationHeader Is Invalid",HttpStatus.INTERNAL_SERVER_ERROR),

    //TOKEN
    INVALID_TOKEN(1006,"Token is Invalid ",HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED(1007,"Access Token is Expired",HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(1007,"Refresh Token is Expired ",HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REVOKED(1008,"Refresh Token is Revoked",HttpStatus.UNAUTHORIZED)
    ;
     String message;
     int code;
     HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.message = message;
        this.code = code;
        this.httpStatus = httpStatus;
    }


}