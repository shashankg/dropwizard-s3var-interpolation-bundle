package com.shash.dropwizard.interpolation;

/**
 * @author shashank.g
 */
public class S3VarInterpolationException extends RuntimeException {

    /**
     * @param errorMessage ::
     */
    public S3VarInterpolationException(final String errorMessage) {
        super(errorMessage);
    }

    /**
     * @param errorMessage ::
     * @param exception    :: exception
     */
    public S3VarInterpolationException(final String errorMessage, final Exception exception) {
        super(errorMessage, exception);
    }
}
