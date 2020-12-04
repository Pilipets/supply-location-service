package uber.location_service.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {
   private Map<String, Object> getBasicExceptionBody() {
      Map<String, Object> exBody = new HashMap<>();
      exBody.put("timestamp", LocalDateTime.now());
      return exBody;
   }

   @ExceptionHandler(JsonProcessingException.class)
   public ResponseEntity<Object> handleJsonException(JsonProcessingException ex, WebRequest webRequest) {
      Map<String, Object> body = getBasicExceptionBody();
      body.put("message", ex.getOriginalMessage());

      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<Object> handleAnyException(
         Exception ex, WebRequest webRequest) {

      Map<String, Object> body = getBasicExceptionBody();
      body.put("message", ex.getMessage());
      body.put("details", "Something bad happened - unable to identify");

      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
   }
}