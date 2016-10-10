## Introduction ##
Interpolates variables from s3 YAML file in Dropwizard YAML configurations. The interpolation happens at runtime. It downloads the YAML file from s3, resolves the variable and interpolates it in the dropwizard configuration. One should have the aws credentials defined in the credential file (~/.aws/credentials) on which the application is running. 

## Usage ##
### In your Application class ###
```java
public void initialize(final Bootstrap bootstrap ) {
	bootstrap.addBundle( new S3VariableInterpolationBundle() );
	...
	...
}
```
### S3 variable interpolation ###
One can define ${S3_INTERPOLATION_VARIABLE_NAME} and provide s3 config in YAML file and this will be picked up from s3 YAML file.

Example config for s3 interpolation: 
```yaml
s3InterpolationConf:
  bucket: dev/my-bucket
  key: configuration.yml
  version: XUIA80A (optional)
  region: AP_SOUTHEAST_1 (default) (optional)
  s3VarPrefix: MY_PREFIX (optional) (default is S3_INTERPOLATION) - case sensitive

database:
  driverClass: com.mysql.jdbc.Driver
  user: ${S3_INTERPOLATION_DB_USER}
  password: ${MY_PREFIX_DB_PASSWORD} (in case of custom defined prefix)
```  

## Notes ##
* AWS credential for the bucket shoud be present for the host on which the application is running.
```
Create your credentials file at ~/.aws/credentials
and save the following lines after replacing the underlined values with your 
own.

[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
```

* This bundle will fail fast, i.e. if an environment variable is not found, it will throw an `S3VarInterpolationException`:

```
S3VarInterpolationException: The environment variable 'S3_INTERPOLATION_DB_USER' is not defined; could not substitute the expression '${S3_INTERPOLATION_DB_USER}'.
```
* DEFAULT PREFIX - S3_INTERPOLATION has to be attached to the variables which need to be picked from s3 YAML file. Variable name is YAML will be - "S3_INTERPOLATION_VAR"
* One can also define custom prefix in the config.

###Build instructions
  - Clone the source:

        git clone https://github.com/shashankg/dropwizard-s3var-interpolation-bundle.git

  - Build

        mvn install

# Repo
```
<repository>
    <id>clojars</id>
    <name>Clojars repository</name>
    <url>https://clojars.org/repo</url>
</repository>
```
# Dependency

## Maven
```
<dependency>
     <groupId>com.shash</groupId>
     <artifactId>dropwizard-s3var-interpolation-bundle</artifactId>
     <version>1.0</version>
</dependency>
```
## Leiningen
```
[com.shash/dropwizard-s3var-interpolation-bundle "1.0"]
```

## Gradle
```
compile "com.shash:dropwizard-s3var-interpolation-bundle:1.0"
```