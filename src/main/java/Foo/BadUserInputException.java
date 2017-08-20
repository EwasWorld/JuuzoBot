package Foo;

public class BadUserInputException extends IllegalArgumentException {
    public BadUserInputException() {
    }


    public BadUserInputException(String s) {
        super(s);
    }
}
