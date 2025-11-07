package root.infrastructure.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleUnexpectedException(Exception e) {
		var message = "Internal server error: %s".formatted(e.getMessage());
		log.error(message, e);
		return message;
	}

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNotFoundException(Exception e) {
		var message = "Resource is not found: %s".formatted(e.getMessage());
		log.warn(message, e);
		return message;
	}

	@ExceptionHandler({
			IllegalArgumentException.class,
			ConstraintViolationException.class,
			MethodArgumentNotValidException.class,
			MissingServletRequestParameterException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleValidationException(Exception e) {
		var message = "Validation failure: %s".formatted(e.getMessage());
		log.warn(message, e);
		return message;
	}
}
