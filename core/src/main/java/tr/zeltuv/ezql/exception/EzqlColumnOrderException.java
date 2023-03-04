package tr.zeltuv.ezql.exception;

public class EzqlColumnOrderException extends RuntimeException {
    public EzqlColumnOrderException(int value, int column) {
        super("The amount of values ("+value+") is not equal to the amount of columns ("+column+").");
    }
}
