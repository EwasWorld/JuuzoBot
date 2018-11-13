package ExceptionsBox;

public class ContactEwaException extends IllegalStateException {
    public ContactEwaException() {
        super();
    }


    public ContactEwaException(String s) {
        super(s + "\nThis is very bad, contact Ewa");
    }
}
