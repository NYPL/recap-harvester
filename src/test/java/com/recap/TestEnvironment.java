package com.recap;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.recap.config.EnvironmentVariableNames;
import com.recap.constants.Constants;


public class TestEnvironment {

  @Rule
  public final EnvironmentVariables envVariables = new EnvironmentVariables();

  public TestEnvironment() {
    envVariables.set(EnvironmentVariableNames.KINESIS_BIB_STREAM, "mockKinesisStream");
    envVariables.set(EnvironmentVariableNames.KINESIS_ITEM_STREAM, "mockKinesisStream");
    envVariables.set(EnvironmentVariableNames.PLATFORM_BASE_API_PATH, "mockBaseApiPath");
    envVariables.set(EnvironmentVariableNames.BIB_SCHEMA_PATH, "mockSchema");
    envVariables.set(EnvironmentVariableNames.ITEM_SCHEMA_PATH, "mockSchema");
    envVariables.set(EnvironmentVariableNames.ONLY_DO_UPDATES, "mockOnlyDoUpdates");
    
    envVariables.set(EnvironmentVariableNames.S3_ACCESS_KEY, "s3Key");
    envVariables.set(EnvironmentVariableNames.S3_SECRET_KEY, "s3Secret");
    envVariables.set(EnvironmentVariableNames.S3_BUCKET, "s3Bucket");
    envVariables.set(EnvironmentVariableNames.S3_BASE_LOCATION, "mockS3");

    envVariables.set(Constants.DOWNLOADED_UPDATES_ACCESSION_DIR, "mockDir");

    envVariables.set(EnvironmentVariableNames.ACCESSION_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.ACCESSION_PROCESSED_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.DEACCESSION_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.DEACCESSION_PROCESSED_DIRECTORY, "mockDir");

    envVariables.set(EnvironmentVariableNames.NYPL_OAUTH_KEY, "mockKey");
    envVariables.set(EnvironmentVariableNames.NYPL_OAUTH_SECRET, "mockSecret");
    envVariables.set(EnvironmentVariableNames.NYPL_OAUTH_URL, "mockUrl");
  }

}
