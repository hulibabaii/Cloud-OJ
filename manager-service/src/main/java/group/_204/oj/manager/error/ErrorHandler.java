package group._204.oj.manager.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<String> sqlErrorHandler(SQLIntegrityConstraintViolationException e) {
        int code = e.getErrorCode();
        log.error(String.valueOf(e.getMessage()));

        HttpStatus status;

        if (code == 1062) {
            status = HttpStatus.CONFLICT;
        } else if (code == 1048) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).build();
    }
}
