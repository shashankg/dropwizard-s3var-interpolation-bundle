package com.shash.dropwizard.interpolation;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shashank.g
 */
@Getter
@Setter
public class S3InterpolationConfig {
    private String bucket;
    private String key;
    private int version;
    private String region;
}
