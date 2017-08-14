# recap-harvester

This application parses a directory of SCSB XML, provided by HTC
and publishes messages to two kinesis streams.

This is for the initial load of data.
TODO: note saying that this requires manual downloading & unzipping from SFTP.

## Installing / Building Locally

### Environment Variables

The following environment variables need to be set.

```
AWS_ACCESS_KEY_ID=[used-to-publish-to-kinesis]
AWS_SECRET_ACCESS_KEY=[used-to-publish-to-kinesis]
bibSchemaAPI=https://[domain.example.com]/api/v0.1/current-schemas/BibPostRequest
itemSchemaAPI=https://[domain.example.com]/api/v0.1/current-schemas/ItemPostRequest
kinesisBibStream=[snip]
kinesisItemStream=[snip]
scsbexportstagingLocation=/var/app/current/scsbxml
```

...can we add specific IDE instructions...(or things useful to future maintainers)

## Deploying to Elastic Beanstalk

### Initial Deploy

1.  `mvn clean package`
1.  `eb init Recap-Harvester --profile [profile name]`
1.  Create application

  ```bash
  eb create recap-harvester-[environment] \
      --instance_type m4.large \
      --instance_profile cloudwatchable-beanstalk \
      --cname recap-harvester-[environment] \
      --vpc.id public-vpc \
      --vpc.elbsubnets public-subnet-id-1 \
      --vpc.ec2subnets private-subnet-id-1 \
      --tags Project=Discovery,harvester=recap_harvester \
      --keyname dgdvteam \
      --scale 1 \
      --envvars KEYFROMABOVE="value",KEYFROMABOVE2="value" \
      --profile your-aws-profile-name
  ```
