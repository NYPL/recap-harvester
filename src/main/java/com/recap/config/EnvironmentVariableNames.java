package com.recap.config;

public class EnvironmentVariableNames {

  public static final String KINESIS_BIB_STREAM = "kinesisBibStream";

  public static final String KINESIS_ITEM_STREAM = "kinesisItemStream";

  public static final String BIB_SCHEMA_PATH = "bibSchemaPath";

  public static final String ITEM_SCHEMA_PATH = "itemSchemaPath";

  public static final String ONLY_DO_UPDATES = "onlyDoUpdates";

  public static final String SCSB_EXPORT_STAGING_LOCATION = "scsbexportstagingLocation";
  
  public static final String FTP_HOST = "ftpHostName";
  
  public static final String FTP_PORT = "ftpPort";

  public static final String FTP_BASE_LOCATION = "ftpBaseLocation";

  public static final String FTP_PRIVATE_KEY_FILE_LOCATION = "ftpPrivateKeyFileLocation";

  public static final String FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY =
      "ftpCompressedFilesProcessedDirectory";

  public static final String FTP_COMPRESSED_FILES_ERROR_DIRECTORY =
      "ftpCompressedFilesFailedDirectory";

  public static final String ACCESSION_DIRECTORY = "accessionDirectory";

  public static final String DEACCESSION_DIRECTORY = "deaccessionDirectory";

  public static final String PLATFORM_BASE_API_PATH = "platformAPIBasePath";

  public static final String NYPL_OAUTH_URL = "NyplOAuthUrl";

  public static final String NYPL_OAUTH_KEY = "NyplOAuthKey";

  public static final String NYPL_OAUTH_SECRET = "NyplOAuthSecret";

}
