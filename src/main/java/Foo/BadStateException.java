package Foo;

public class BadStateException extends IllegalStateException {
    public BadStateException() {
    }


    public BadStateException(String s) {
        super(s);
    }
}
