package com.recap;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.recap.config.EnvironmentVariableNames;


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
    envVariables.set(EnvironmentVariableNames.FTP_HOST, "mockFtp");
    envVariables.set(EnvironmentVariableNames.FTP_PORT, "mockFtp");
    envVariables.set(EnvironmentVariableNames.FTP_BASE_LOCATION, "mockFtp");
    envVariables.set(EnvironmentVariableNames.FTP_PRIVATE_KEY_FILE_LOCATION, "mockFtp");
    envVariables.set(EnvironmentVariableNames.FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY, "mockFtp");

    envVariables.set(EnvironmentVariableNames.UNCOMPRESSED_FILES_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.FTP_COMPRESSED_FILES_ERROR_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.ACCESSION_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.DEACCESSION_DIRECTORY, "mockDir");
    envVariables.set(EnvironmentVariableNames.NYPL_OAUTH_KEY, "mockKey");
    envVariables.set(EnvironmentVariableNames.NYPL_OAUTH_SECRET, "mockSecret");
    envVariables.set(EnvironmentVariableNames.NYPL_OAUTH_URL, "mockUrl");
  }

}
