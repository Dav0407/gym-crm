package com.epam.gym_crm.handler;

import com.epam.gym_crm.dto.response.ExceptionResponse;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.InvalidUserCredentialException;
import com.epam.gym_crm.exception.ResourceNotFoundException;
import com.epam.gym_crm.exception.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static com.epam.gym_crm.handler.BusinessErrorCodes.INTERNAL_ERROR;
import static com.epam.gym_crm.handler.BusinessErrorCodes.RESOURCE_NOT_FOUND;
import static com.epam.gym_crm.handler.BusinessErrorCodes.USER_NOT_FOUND;
import static com.epam.gym_crm.handler.BusinessErrorCodes.USER_UNAUTHORIZED;
import static com.epam.gym_crm.handler.BusinessErrorCodes.VALIDATION_FAILED;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log LOG = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        LOG.error("MethodArgumentNotValidException: ", exception);

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return createExceptionResponse(
                VALIDATION_FAILED.getHttpStatus(),
                VALIDATION_FAILED.getCode(),
                VALIDATION_FAILED.getDescription(),
                "One or more fields are invalid.",
                errors
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        LOG.error("ResourceNotFoundException: ", exception);

        return createExceptionResponse(
                RESOURCE_NOT_FOUND.getHttpStatus(),
                RESOURCE_NOT_FOUND.getCode(),
                RESOURCE_NOT_FOUND.getDescription(),
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidUserCredentialException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidUserCredentialException(InvalidUserCredentialException exception) {
        LOG.error("InvalidUserCredentialException: ", exception);

        return createExceptionResponse(
                USER_UNAUTHORIZED.getHttpStatus(),
                USER_UNAUTHORIZED.getCode(),
                USER_UNAUTHORIZED.getDescription(),
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidPasswordException(InvalidPasswordException exception) {
        LOG.error("InvalidPasswordException: ", exception);

        return createExceptionResponse(
                VALIDATION_FAILED.getHttpStatus(),
                VALIDATION_FAILED.getCode(),
                VALIDATION_FAILED.getDescription(),
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(UserNotFoundException exception) {
        LOG.error("UserNotFoundException: ", exception);

        return createExceptionResponse(
                USER_NOT_FOUND.getHttpStatus(),
                USER_NOT_FOUND.getCode(),
                USER_NOT_FOUND.getDescription(),
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
        LOG.error("EntityNotFoundException: ", exception);

        return createExceptionResponse(
                USER_NOT_FOUND.getHttpStatus(),
                USER_NOT_FOUND.getCode(),
                USER_NOT_FOUND.getDescription(),
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        LOG.error("MissingRequestHeaderException: ", exception);

        String errorMessage = String.format("Required header '%s' is missing", exception.getHeaderName());

        return createExceptionResponse(
                USER_UNAUTHORIZED.getHttpStatus(),
                USER_UNAUTHORIZED.getCode(),
                USER_UNAUTHORIZED.getDescription(),
                errorMessage,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        LOG.error("An exception occurred: ", exception);

        return createExceptionResponse(
                INTERNAL_ERROR.getHttpStatus(),
                INTERNAL_ERROR.getCode(),
                INTERNAL_ERROR.getDescription(),
                exception.getMessage(),
                null
        );
    }

    /**
     * Creates a standardized exception response entity.
     *
     * @param httpStatus              HTTP status for the response
     * @param businessErrorCode       Business error code
     * @param businessErrorDescription Business error description
     * @param errorMessage            Error message
     * @param validationErrors        Optional map of field validation errors
     * @return ResponseEntity with constructed ExceptionResponse
     */
    private ResponseEntity<ExceptionResponse> createExceptionResponse(
            HttpStatus httpStatus,
            int businessErrorCode,
            String businessErrorDescription,
            String errorMessage,
            Map<String, String> validationErrors) {

        ExceptionResponse.ExceptionResponseBuilder responseBuilder = ExceptionResponse.builder()
                .businessErrorCode(businessErrorCode)
                .businessErrorDescription(businessErrorDescription)
                .errorMessage(errorMessage);

        if (validationErrors != null) {
            responseBuilder.validationErrors(validationErrors);
        }

        return ResponseEntity.status(httpStatus).body(responseBuilder.build());
    }
}