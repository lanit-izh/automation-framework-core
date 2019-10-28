package ru.lanit.at.exceptions;

public class FrameworkRuntimeException extends RuntimeException {
    public FrameworkRuntimeException() {
    }

    public FrameworkRuntimeException(String message) {
        super(message);
    }

    public FrameworkRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameworkRuntimeException(Throwable cause) {
        super(cause);
    }

    public FrameworkRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
