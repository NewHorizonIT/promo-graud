package group2d.promo_graud.shared.exception;


import group2d.promo_graud.modules.auth.AuthErrorCode;
import group2d.promo_graud.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

// Bắt toàn bộ lỗi (exception) từ các API
@ControllerAdvice
public class GlobalExceptionHandler {

  // 1. Lỗi phân quyền (Không có quyền truy cập API)
  @ExceptionHandler(value = AccessDeniedException.class)
  ResponseEntity<ApiResponse<String>> handlingAccessDeniedException(AccessDeniedException accessDeniedException){
    BaseErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;
    return ResponseEntity.status(errorCode.getStatusCode()).body(
      ApiResponse.<String>builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build()
    );
  }

  // 2. Lỗi nghiệp vụ (Dev chủ động ném ra, vd: Sai mật khẩu, User không tồn tại)
  @ExceptionHandler(value = AppException.class)
  ResponseEntity<ApiResponse<String>> handlingAppException(AppException  exception){
    BaseErrorCode errorCode = exception.getErrorCode();
    ApiResponse<String> apiResponse = new ApiResponse<>();
    apiResponse.setCode(errorCode.getCode());
    apiResponse.setMessage(errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
  }

  // 3. Lỗi dữ liệu đầu vào (Gửi request body sai định dạng @Valid)
  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<String>> handlingValidation(MethodArgumentNotValidException exception){
    ApiResponse<String> apiResponse = new ApiResponse<>();
    String errorMessage = exception.getBindingResult().getFieldError() != null
      ? exception.getBindingResult().getFieldError().getDefaultMessage()
      : "Data is not valid";
    return ResponseEntity.badRequest().body(
      ApiResponse.<String>builder()
        .code(1002)
        .message(errorMessage)
        .build());
  }

  // 4. Lỗi hệ thống (Bắt các lỗi bất ngờ )
  @ExceptionHandler(value = Exception.class)
  ResponseEntity<ApiResponse<String>> handlingException(Exception  exception){
    exception.printStackTrace();
    BaseErrorCode errorCode = AuthErrorCode.UNCATEGORIZED_EXCEPTION;
    return ResponseEntity.status(errorCode.getStatusCode()).body(
      ApiResponse.<String>builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build()
    );
  }
}
