package com.dronze.blueweave.provider;

/**
 * Created by claytongraham on 9/17/16.
 */
public class DataSetProviderException extends Exception {
    public DataSetProviderException() {
    }

    public DataSetProviderException(String message) {
        super(message);
    }

    public DataSetProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSetProviderException(Throwable cause) {
        super(cause);
    }

    public DataSetProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
