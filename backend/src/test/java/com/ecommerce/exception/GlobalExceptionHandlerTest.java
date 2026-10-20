package com.ecommerce.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsUnmappedPathsToNotFound() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNoHandler(new NoResourceFoundException(HttpMethod.GET, "api/removed-endpoint"));

        assertStatus(response, HttpStatus.NOT_FOUND);
        assertEquals("Resource not found", message(response));
    }

    @Test
    void mapsWrongHttpMethodToMethodNotAllowed() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("DELETE"));

        assertStatus(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void mapsAccessDeniedToForbiddenWithoutLeakingTheRule() {
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(
                new AccessDeniedException("Access denied for method with role check hasRole('ADMIN')"));

        assertStatus(response, HttpStatus.FORBIDDEN);
        assertEquals("Access denied", message(response));
        assertFalse(message(response).contains("hasRole"));
    }

    @Test
    void mapsDomainForbiddenToForbiddenAndKeepsTheMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleForbidden(
                new ForbiddenException("You can only review products you have purchased"));

        assertStatus(response, HttpStatus.FORBIDDEN);
        assertEquals("You can only review products you have purchased", message(response));
    }

    @Test
    void mapsMissingResourceToNotFound() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Product", 42L));

        assertStatus(response, HttpStatus.NOT_FOUND);
        assertEquals("Product not found with id: 42", message(response));
    }

    @Test
    void mapsConflictAndBadRequestAndBadCredentials() {
        assertStatus(handler.handleConflict(new ConflictException("Email already in use")),
                HttpStatus.CONFLICT);
        assertStatus(handler.handleBadRequest(new BadRequestException("Invalid role")),
                HttpStatus.BAD_REQUEST);

        ResponseEntity<Map<String, Object>> unauthorized =
                handler.handleBadCredentials(new BadCredentialsException("secret detail"));
        assertStatus(unauthorized, HttpStatus.UNAUTHORIZED);
        assertEquals("Invalid email or password", message(unauthorized));
    }

    @Test
    void mapsMalformedRequestBodyToBadRequest() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMalformedRequest(mock(HttpMessageNotReadableException.class));

        assertStatus(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsUnconvertiblePathParameterToBadRequest() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMalformedRequest(mock(MethodArgumentTypeMismatchException.class));

        assertStatus(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsUnexpectedErrorsToInternalServerErrorWithoutLeakingDetails() {
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(
                new IllegalStateException("connection pool exhausted at com.zaxxer.hikari.Foo"));

        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals("An unexpected error occurred", message(response));
        assertFalse(message(response).contains("hikari"));
    }

    private void assertStatus(ResponseEntity<Map<String, Object>> response, HttpStatus expected) {
        assertEquals(expected, response.getStatusCode());
        assertEquals(expected.value(), response.getBody().get("status"));
        assertEquals(expected.getReasonPhrase(), response.getBody().get("error"));
    }

    private String message(ResponseEntity<Map<String, Object>> response) {
        return (String) response.getBody().get("message");
    }
}
