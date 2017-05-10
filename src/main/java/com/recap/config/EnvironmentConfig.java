package com.recap.config;

public final class EnvironmentConfig {

  public static final String KINESIS_BIB_STREAM = System.getenv("kinesisBibStream");

  public static final String KINESIS_ITEM_STREAM = System.getenv("kinesisItemStream");

  public static final String BIB_SCHEMA_API = System.getenv("bibSchemaAPI");

  public static final String ITEM_SCHEMA_API = System.getenv("itemSchemaAPI");

}
