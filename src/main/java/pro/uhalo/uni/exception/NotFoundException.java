package pro.uhalo.uni.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * NotFoundException.
 *
 * @author 小莫唐尼
 */
public class NotFoundException extends ResponseStatusException {
    public NotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}