package br.com.threadstech.stockfy.modules.users.presentation.exception;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String detail = messageSource.getMessage("validation.error.message", null, LocaleContextHolder.getLocale());
        
        ApiErrorResponse response = new ApiErrorResponse(
            "about:blank",
            "Validation Error",
            HttpStatus.BAD_REQUEST.value(),
            detail,
            null,
            ex.getBindingResult().getFieldErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
