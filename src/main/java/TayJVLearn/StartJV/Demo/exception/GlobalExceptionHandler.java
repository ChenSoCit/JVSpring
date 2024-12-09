package TayJVLearn.StartJV.Demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationEception(Exception e, WebRequest request){ // nhan request tu web
        ErrorResponse errorRespones = new ErrorResponse();
        errorRespones.setTimestamp(new Date());
        errorRespones.setStatus(HttpStatus.BAD_REQUEST.value());
        errorRespones.setPath(request.getDescription(false).replace("uri=",""));
        errorRespones.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        String message = e.getMessage();
        int start = message.lastIndexOf("[");
        int end = message.lastIndexOf("]");
        message = message.substring(start + 1, end - 1);
        errorRespones.setMessage(message);

        return errorRespones;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerErrorEception(Exception e, WebRequest request){ // nhan request tu web
        ErrorResponse errorRespones = new ErrorResponse();
        errorRespones.setTimestamp(new Date());
        errorRespones.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorRespones.setPath(request.getDescription(false).replace("uri=",""));
        errorRespones.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());

        String message = e.getMessage();
        message = "Failed to convert value of type";
        errorRespones.setMessage(message);

        return errorRespones;
    }
}
