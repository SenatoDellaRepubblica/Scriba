package it.senato.areatesti.ebook.scriba.exception;

/**
 * This exception means an interrupted ebook generation
 */
public class TerminatedGenerationException extends ScribaException {

    /**
     * ID
     */
    private static final long serialVersionUID = -2735941762381022323L;


    @Override
    public String getMessage() {
        return "The ebook generation is finished";
    }
}
