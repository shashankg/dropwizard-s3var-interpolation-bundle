package com.shash.dropwizard.interpolation;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author shashank.g
 */
@Slf4j
@SuppressWarnings("unchecked")
public class S3VariableResolver {

    private static final String S3_VAR_INTERPOLATION = "s3InterpolationConf";
    private static final String S3_DEFAULT_REGION = "AP_SOUTHEAST_1";
    private static final String DOWNLOAD_FILE_NAME = "s3variables.tmp";

    private final Yaml yaml;
    private final AmazonS3 client;
    private final ObjectMapper mapper;

    /**
     * Constructor
     */
    public S3VariableResolver(final ObjectMapper mapper, final Yaml yaml) {
          /*
         * Create your credentials file at ~/.aws/credentials
         * and save the following lines after replacing the underlined values with your own.
         *
         * [default]
         * aws_access_key_id = YOUR_ACCESS_KEY_ID
         * aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
         */
        this.client = new AmazonS3Client();

        this.mapper = mapper;
        this.yaml = yaml;
    }

    /**
     * Resolve variables
     *
     * @param s3InterpolationConf ::
     * @return resolved variables
     */
    public Map<String, String> resolve(final S3InterpolationConfig s3InterpolationConf) {
        //
        this.client.setRegion(
                s3InterpolationConf.getRegion() == null
                        ? Region.getRegion(Regions.valueOf(S3_DEFAULT_REGION))
                        : Region.getRegion(Regions.valueOf(s3InterpolationConf.getRegion())
                ));

        final Map<String, String> resolvedVars;
        File downloadedFile = null;
        InputStream downloadStream = null;
        try {
            downloadStream = download(s3InterpolationConf.getBucket(), s3InterpolationConf.getKey());
            downloadedFile = save(downloadStream);
            resolvedVars = getVariableValues(downloadedFile);
        } catch (final Exception e) {
            log.error("Exception - {}", e.getMessage(), e);
            throw new S3VarInterpolationException(e.getMessage());
        } finally {
            if (downloadedFile != null) {
                downloadedFile.delete();
            }
            if (downloadStream != null) {
                IOUtils.closeQuietly(downloadStream, null);
            }
        }
        return resolvedVars;
    }

    /**
     * @param dropwizardConfig :: to parse
     * @return interpolation config
     */
    public S3InterpolationConfig parseS3Config(final Map<String, Object> dropwizardConfig) {
        try {
            return mapper.convertValue(dropwizardConfig.get(S3_VAR_INTERPOLATION), S3InterpolationConfig.class);
        } catch (final Exception e) {
            log.error("Encountered exception while parsing s3 config - {}", e.getMessage(), e);
            throw new S3VarInterpolationException("Unable to parse s3 configuration.");
        }
    }

    /*
     * get variable values from file
     */
    private Map<String, String> getVariableValues(final File downloadFile) throws Exception {
        return yaml.loadAs(new FileInputStream(downloadFile), Map.class);
    }

    /*
     * Download s3 object
     *
     * @param bucket :: to downloadAndSave from
     * @param key    :: s3 key
     * @return input stream
     */
    private InputStream download(final String bucket, final String key) {
        log.info("Fetching object from S3 bucket - {}, key - {}", bucket, key);
        final S3Object s3Object;
        try {
            s3Object = client.getObject(new GetObjectRequest(bucket, key));
        } catch (final Exception e) {
            log.error("S3 Download operation failed. {}", e.getMessage(), e);
            throw new S3VarInterpolationException("S3 downloadAndSave operation failed.");
        }
        log.debug("S3 Downloaded object: {}", s3Object.toString());
        return s3Object.getObjectContent();
    }

    /*
     * Save file
     * @param inputStream :: to save locally
     * @return file
     */
    private File save(final InputStream inputStream) {
        Path path = null;
        FileOutputStream out = null;
        try {
            path = Files.createFile(Paths.get(DOWNLOAD_FILE_NAME, ""));
            out = new FileOutputStream(path.toFile());
        } catch (final IOException e) {
            log.error("Encountered exception. ", e);
            throw new S3VarInterpolationException(e.getMessage());
        } finally {
            if (inputStream != null && out != null) {
                try {
                    IOUtils.copy(inputStream, out);
                    IOUtils.closeQuietly(out, null);
                } catch (final IOException ignore) {
                    // ignoring
                    log.error("Exception - ", ignore);
                }
            }
        }
        return path.toFile();
    }
}
