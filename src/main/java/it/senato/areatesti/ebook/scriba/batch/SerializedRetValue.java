package it.senato.areatesti.ebook.scriba.batch;

import java.io.Serializable;

/**
 * The class to represents the batch processing outcome
 */
public final class SerializedRetValue implements Serializable {
    private static final long serialVersionUID = -1448855294003450400L;

    private String filename;
    private int numRetry;
    private boolean failed;


    public SerializedRetValue(String filename, int numRetry, boolean failed) {
        this.filename = filename;
        this.numRetry = numRetry;
        this.failed = failed;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getNumRetry() {
        return numRetry;
    }

    public void setNumRetry(int numRetry) {
        this.numRetry = numRetry;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}