import lombok.*;

@EqualsAndHashCode(callSuper=false)
@Value class BScannerException extends Exception {

    private static final long serialVersionUID = 1L;
    Integer code;
    
    BScannerException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
