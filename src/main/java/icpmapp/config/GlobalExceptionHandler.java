package icpmapp.config;

import icpmapp.dto.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .error(ex.getReason())
                        .code(status.name())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        
        String code = "INTERNAL_ERROR";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("not found") || message.contains("Not Found")) {
                code = "NOT_FOUND";
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("already exists")) {
                code = "DUPLICATE_KEY";
                status = HttpStatus.CONFLICT;
            } else if (message.contains("required") || message.contains("invalid") || message.contains("Invalid")) {
                code = "VALIDATION_ERROR";
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .error(ex.getMessage())
                        .code(code)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .error("Insufficient permissions")
                        .code("FORBIDDEN")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.warn("File upload too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error("File size exceeds maximum allowed size")
                        .code("FILE_TOO_LARGE")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
