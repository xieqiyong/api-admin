package com.hz.api.admin.client.client.message;

public class SendMessageException extends RuntimeException {
    public SendMessageException() {
        super();
    }

    public SendMessageException(String message) {
        super(message);
    }

    public SendMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendMessageException(Throwable cause) {
        super(cause);
    }

    protected SendMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
