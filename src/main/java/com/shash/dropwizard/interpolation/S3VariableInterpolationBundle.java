package com.shash.dropwizard.interpolation;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author shashank.g
 */
public class S3VariableInterpolationBundle implements Bundle {

    private static final Yaml yaml = new Yaml();

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new VariableProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        bootstrap.getObjectMapper(),
                        yaml
                )
        );
    }

    @Override
    public void run(final Environment environment) {
        // nothing to run
    }
}

