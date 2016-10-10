package com.shash.dropwizard.interpolation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author shashank.g
 */
@Slf4j
public class VariableProvider implements ConfigurationSourceProvider {

    private final ConfigurationSourceProvider baseProvider;
    private final S3VariableResolver s3VariableResolver;
    private final ObjectMapper mapper;
    private final Yaml yaml;

    /**
     * Constructor
     *
     * @param baseProvider ::
     */
    public VariableProvider(final ConfigurationSourceProvider baseProvider, final ObjectMapper mapper, final Yaml yaml) {
        this.mapper = mapper;
        this.yaml = yaml;
        this.baseProvider = baseProvider;
        this.s3VariableResolver = new S3VariableResolver(this.mapper, this.yaml);
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputStream open(final String path) throws IOException {
        final Map<String, Object> dropwizardConf = yaml.loadAs(baseProvider.open(path), Map.class);
        final S3InterpolationConfig s3Config = s3VariableResolver.parseS3Config(dropwizardConf);

        if (s3Config == null) {
            return new ByteArrayInputStream(yaml.dump(dropwizardConf).getBytes());
        }

        final Map<String, String> resolvedS3Variables = s3VariableResolver.resolve(s3Config);
        final StrSubstitutor strSubstitutor = new StrSubstitutor(new S3EnvironmentVariableLookup(resolvedS3Variables));
        final String resolvedConfigStr = strSubstitutor.replace(mapper.writeValueAsString(dropwizardConf));

        final String resolvedConfig = yaml.dump(yaml.loadAs(resolvedConfigStr, Map.class));
        return new ByteArrayInputStream(resolvedConfig.getBytes());
    }
}
