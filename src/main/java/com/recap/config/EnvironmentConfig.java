package com.recap.config;

public final class EnvironmentConfig {

  public static final String KINESIS_BIB_STREAM = System.getenv("kinesisBibStream");

  public static final String KINESIS_ITEM_STREAM = System.getenv("kinesisItemStream");

  public static final String BIB_SCHEMA_API =
      System.getenv("platformAPIBasePath") + System.getenv("bibSchemaPath");

  public static final String ITEM_SCHEMA_API =
      System.getenv("platformAPIBasePath") + System.getenv("itemSchemaPath");

  public static final Boolean ONLY_DO_UPDATES =
      Boolean.valueOf(System.getenv("onlyDoUpdates").toLowerCase().trim());

  public static final String FTP_HOST = System.getenv("ftpHostName");

  public static final String FTP_PORT = System.getenv("ftpPort");

  public static final String FTP_BASE_LOCATION = System.getenv("ftpBaseLocation");

  public static final String FTP_PRIVATE_KEY_FILE_LOCATION =
      System.getenv("ftpPrivateKeyFileLocation");

  public static final String FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY =
      System.getenv("ftpCompressedFilesProcessedDirectory");

  public static final String UNCOMPRESSED_FILES_DIRECTORY =
      System.getenv("uncompressedFilesDirectoryForUpdates");

  public static final String FTP_COMPRESSED_FILES_ERROR_DIRECTORY =
      System.getenv("ftpCompressedFilesFailedDirectory");

  public static final String ACCESSION_DIRECTORY = System.getenv("accessionDirectory");

  public static final String DEACCESSION_DIRECTORY = System.getenv("deaccessionDirectory");

  public static final String PLATFORM_BASE_API_PATH = System.getenv("platformAPIBasePath");

}
