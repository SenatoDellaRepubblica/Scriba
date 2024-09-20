package it.senato.areatesti.ebook.scriba.exception;

class ScribaException extends Exception {

    ScribaException() {
        super();
    }

    public ScribaException(String message) {
        super(message);
    }

    public ScribaException(Throwable cause) {
        super(cause);
    }

    public ScribaException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScribaException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}