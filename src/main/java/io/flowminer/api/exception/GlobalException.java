package io.flowminer.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException exception) {
        Map<String, String> map = new HashMap<>();
        map.put("error", exception.getMessage());
        return new ResponseEntity<>(map,HttpStatus.BAD_REQUEST);
    }
}
