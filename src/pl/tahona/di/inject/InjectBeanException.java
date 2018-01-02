package pl.tahona.di.inject;

public class InjectBeanException extends RuntimeException {
    public InjectBeanException(final String message) {
        super(message);
    }

    public InjectBeanException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
