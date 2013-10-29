package org.arper.tlaux;

public class TLAMalformedDataGridException extends Exception {

    private static final long serialVersionUID = 1L;

    public TLAMalformedDataGridException(String message) {
        super(message);
    }

    public TLAMalformedDataGridException(String message, Throwable cause) {
        super(message, cause);
    }
}
