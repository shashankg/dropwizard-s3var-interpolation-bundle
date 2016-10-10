package com.shash.dropwizard.interpolation;

import org.apache.commons.lang3.text.StrLookup;

import java.util.Map;

/**
 * @author shashank.g
 */
public class S3EnvironmentVariableLookup extends StrLookup {

    private static final String S3_PREFIX = "S3_INTERPOLATION";
    private final Map<String, String> s3Variables;

    /**
     * Constructor
     *
     * @param s3Variables resolved
     */
    public S3EnvironmentVariableLookup(final Map<String, String> s3Variables) {
        this.s3Variables = s3Variables;
    }

    @Override
    public String lookup(final String key) {
        if (key != null && key.toUpperCase().trim().startsWith(S3_PREFIX)) {
            if (s3Variables == null) {
                throw new S3VarInterpolationException("The s3 variable '" + key
                        + "' is not defined. " + "'${"
                        + key + "}'." +
                        " Is this available on s3 file or you missed to provide the config for s3?");
            }

            final String value = s3Variables.get(key);
            if (value == null) {
                throw new S3VarInterpolationException("The s3 variable '" + key
                        + "' is not defined in s3 file; could not substitute the expression '${"
                        + key + "}'.");
            }
            return value.trim();
        }
        return null;
    }
}