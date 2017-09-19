package com.recap.config;

public final class EnvironmentConfig {

  public static final String KINESIS_BIB_STREAM =
      System.getenv(EnvironmentVariableNames.KINESIS_BIB_STREAM);

  public static final String KINESIS_ITEM_STREAM =
      System.getenv(EnvironmentVariableNames.KINESIS_ITEM_STREAM);

  public static final String BIB_SCHEMA_API =
      System.getenv(EnvironmentVariableNames.PLATFORM_BASE_API_PATH)
          + System.getenv(EnvironmentVariableNames.BIB_SCHEMA_PATH);

  public static final String ITEM_SCHEMA_API =
      System.getenv(EnvironmentVariableNames.PLATFORM_BASE_API_PATH)
          + System.getenv(EnvironmentVariableNames.ITEM_SCHEMA_PATH);

  public static final Boolean ONLY_DO_UPDATES =
      Boolean.valueOf(System.getenv(EnvironmentVariableNames.ONLY_DO_UPDATES).toLowerCase().trim());

  public static final String FTP_HOST = System.getenv(EnvironmentVariableNames.FTP_HOST);

  public static final String FTP_PORT = System.getenv(EnvironmentVariableNames.FTP_PORT);

  public static final String FTP_BASE_LOCATION =
      System.getenv(EnvironmentVariableNames.FTP_BASE_LOCATION);

  public static final String FTP_PRIVATE_KEY_FILE_LOCATION =
      System.getenv(EnvironmentVariableNames.FTP_PRIVATE_KEY_FILE_LOCATION);

  public static final String FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY =
      System.getenv(EnvironmentVariableNames.FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY);

  public static final String UNCOMPRESSED_FILES_DIRECTORY =
      System.getenv(EnvironmentVariableNames.UNCOMPRESSED_FILES_DIRECTORY);

  public static final String FTP_COMPRESSED_FILES_ERROR_DIRECTORY =
      System.getenv(EnvironmentVariableNames.FTP_COMPRESSED_FILES_ERROR_DIRECTORY);

  public static final String ACCESSION_DIRECTORY =
      System.getenv(EnvironmentVariableNames.ACCESSION_DIRECTORY);

  public static final String DEACCESSION_DIRECTORY =
      System.getenv(EnvironmentVariableNames.DEACCESSION_DIRECTORY);

  public static final String PLATFORM_BASE_API_PATH =
      System.getenv(EnvironmentVariableNames.PLATFORM_BASE_API_PATH);

  public static final String NYPL_OAUTH_URL =
      System.getenv(EnvironmentVariableNames.NYPL_OAUTH_URL);

  public static final String NYPL_OAUTH_KEY =
      System.getenv(EnvironmentVariableNames.NYPL_OAUTH_KEY);

  public static final String NYPL_OAUTH_SECRET =
      System.getenv(EnvironmentVariableNames.NYPL_OAUTH_SECRET);

}
