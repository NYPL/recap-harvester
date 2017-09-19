package com.recap.updater.deletions;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.recap.config.EnvironmentConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DeleteInfoProcessor implements Processor {

  boolean doCleanUp = false;

  private static Logger logger = LoggerFactory.getLogger(DeleteInfoProcessor.class);

  private static final String JSON_EXTENSION = ".json";
  private static final String DELETE_ALL_ITEMS = "deleteAllItems";
  private static final String OWNING_INSTITUTION_CODE = "owningInstitutionCode";
  private static final String OWNING_INSTITUTION_BIB_ID = "owningInstitutionBibId";
  private static final String OWNING_INSTITUTION_ITEM_ID = "owningInstitutionItemId";
  private static final String ITEMS = "items";
  private static final String BIBS = "bibs";
  private static final String DATA = "data";

  private String token;
  private String tokenType;


  public DeleteInfoProcessor(boolean doCleanUp) {
    this.doCleanUp = doCleanUp;
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
    try {
      File dirJsonFileThatHasDeleteInfo = exchange.getIn().getBody(File.class);

      if (dirJsonFileThatHasDeleteInfo != null) {
        File[] files = dirJsonFileThatHasDeleteInfo.listFiles();

        Map<String, List<String>> bibsAndItemsDeleted = getAllDeletedBibsAndItems(files);

        exchange.getIn().setBody(bibsAndItemsDeleted, Map.class);
      }
    } catch (Exception e) {
      logger.error("Error occurred while extracting deleted bibs and items info from json file - ",
          e);
      throw new RecapHarvesterException(
          "Error while extracting bibs and items info from json file");
    }
  }

  private Map<String, List<String>> getAllDeletedBibsAndItems(File[] files)
      throws RecapHarvesterException {
    try {
      List<String> deletedBibs = new ArrayList<>();
      List<String> deletedItems = new ArrayList<>();

      for (File file : files) {
        if (file.getName().trim().endsWith(JSON_EXTENSION)) {
          ObjectMapper mapper = new ObjectMapper();
          List<Map<String, Map<String, Object>>> deletedJSON = mapper.readValue(file,
              mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

          for (Map<String, Map<String, Object>> bib : deletedJSON) {
            Map<String, Object> theBib = bib.get(Constants.BIB);
            Boolean deleteAllItems = (Boolean) theBib.get(DELETE_ALL_ITEMS);

            if (deleteAllItems) {
              List<String> itemIds = getItemIds(theBib.get(OWNING_INSTITUTION_CODE).toString(),
                  theBib.get(OWNING_INSTITUTION_BIB_ID).toString());
              // First delete all items for the bib
              deleteAllItems(itemIds, theBib.get(OWNING_INSTITUTION_BIB_ID).toString(),
                  theBib.get(OWNING_INSTITUTION_CODE).toString(), deletedItems);
              // Then delete the bib itself
              deleteBib(theBib.get(OWNING_INSTITUTION_BIB_ID).toString(),
                  theBib.get(OWNING_INSTITUTION_CODE).toString(), deletedBibs);
            } else {
              List<Map<String, String>> items = (List) theBib.get(ITEMS);

              for (Map<String, String> item : items) {
                deletedItems.add(transformToDeleted(item, Constants.ITEM,
                    (String) theBib.get(OWNING_INSTITUTION_CODE)));
              }
            }

          }
        }
        if (doCleanUp) {
          file.delete();
        }
      }

      Map<String, List<String>> bibsAndItemsDeleted = new HashMap<>();
      bibsAndItemsDeleted.put(Constants.DELETED_BIBS, deletedBibs);
      bibsAndItemsDeleted.put(Constants.DELETED_ITEMS, deletedItems);

      return bibsAndItemsDeleted;
    } catch (JsonParseException e) {
      String errMessage =
          "Json parsing failed while getting all deleted bibs and items - " + e.getMessage();
      logger.error(errMessage + " ", e);
      throw new RecapHarvesterException(errMessage);
    } catch (JsonMappingException e) {
      String errMessage =
          "Json mapping failed while getting all deleted bibs and items - " + e.getMessage();
      logger.error(errMessage + " ", e);
      throw new RecapHarvesterException(errMessage);
    } catch (IOException e) {
      String errMessage =
          "IOException occurred while getting all deleted bibs and items - " + e.getMessage();
      logger.error(errMessage + " ", e);
      throw new RecapHarvesterException(errMessage);
    }

  }

  private void deleteAllItems(List<String> itemIds, String owningInstitutionBibId,
      String owningInstitutionCode, List<String> deletedItems) throws RecapHarvesterException {
    for (String itemId : itemIds) {
      Map<String, String> itemRecord = new HashMap<>();
      itemRecord.put(OWNING_INSTITUTION_ITEM_ID, itemId);
      itemRecord.put(OWNING_INSTITUTION_BIB_ID, owningInstitutionBibId);
      deletedItems.add(transformToDeleted(itemRecord, Constants.ITEM, owningInstitutionCode));
    }
  }

  private void deleteBib(String owningInstitutionBibId, String owningInstitutionCode,
      List<String> deletedBibs) throws RecapHarvesterException {
    Map<String, String> bibRecord = new HashMap<>();
    bibRecord.put(OWNING_INSTITUTION_BIB_ID, owningInstitutionBibId);
    deletedBibs.add(transformToDeleted(bibRecord, Constants.BIB, owningInstitutionCode));
  }

  public List<String> getItemIds(String owningInstitutionCode, String owningInstitutionBibId)
      throws RecapHarvesterException {
    RetryTemplate retryTemplate = new RetryTemplate();
    List<String> itemIds =
        retryTemplate.execute(new RetryCallback<List<String>, RecapHarvesterException>() {

          @Override
          public List<String> doWithRetry(RetryContext context) throws RecapHarvesterException {
            List<String> itemIds = new ArrayList<>();
            try {
              RestTemplate restTemplate = new RestTemplate();
              HttpHeaders httpHeaders = new HttpHeaders();
              httpHeaders.add("Authorization", tokenType + " " + token);
              HttpEntity<String> httpEntity = new HttpEntity<>("parameters", httpHeaders);
              String url = EnvironmentConfig.PLATFORM_BASE_API_PATH + "/" + BIBS + "/"
                  + Constants.NYPL_SOURCE_RECAP + "-" + owningInstitutionCode.toLowerCase() + "/"
                  + owningInstitutionBibId + "/" + ITEMS;
              ResponseEntity<String> response =
                  restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
              Map<String, List<Map<String, Object>>> data =
                  new ObjectMapper().readValue(response.getBody(), Map.class);
              List<Map<String, Object>> items = data.get(DATA);

              for (Map<String, Object> item : items) {
                itemIds.add(item.get("id").toString());
              }
            } catch (JsonMappingException e) {
              String errMessage = "JSON mapping failed while getting item ids " + e.getMessage();
              logger.error(errMessage + " ", e);
              throw new RecapHarvesterException(errMessage);
            } catch (JsonParseException e) {
              String errMessage = "JSON parsing failed while getting item ids " + e.getMessage();
              logger.error(errMessage + " ", e);
              throw new RecapHarvesterException(errMessage);
            } catch (IOException e) {
              String errMessage = "IOException occurred while getting item ids " + e.getMessage();
              logger.error(errMessage + " ", e);
              throw new RecapHarvesterException(errMessage);
            } catch (HttpClientErrorException e) {
              if (e.getRawStatusCode() == 401) {
                setNewToken();
                throw new RecapHarvesterException(e.getMessage());
              } else if (e.getRawStatusCode() == 404) {
                logger.error("No items found for the bib");
              }

            }
            return itemIds;
          }
        });
    return itemIds;
  }

  private void setNewToken() throws RecapHarvesterException {
    try {
      OAuth2Client oAuth2Client =
          new OAuth2Client(EnvironmentConfig.NYPL_OAUTH_URL, EnvironmentConfig.NYPL_OAUTH_KEY,
              EnvironmentConfig.NYPL_OAUTH_SECRET, "client_credentials");
      OAuth2AccessToken oAuth2AccessToken = oAuth2Client.createAndGetOAuth2AccessToken();
      token = oAuth2AccessToken.getValue();
      tokenType = oAuth2AccessToken.getTokenType();
    } catch (HttpClientErrorException e) {
      throw new RecapHarvesterException("Unable to get NYPL token - " + e.getMessage());
    }
  }

  private String transformToDeleted(Map<String, String> record, String bibOrItem,
      String owningInstitution) throws RecapHarvesterException {

    try {
      Map<String, Object> deletedRecord = new HashMap<>();
      DateFormat deleteDateFormat = new SimpleDateFormat("YYYY-MM-dd");

      deletedRecord.put("nyplSource",
          Constants.NYPL_SOURCE_RECAP + "-" + owningInstitution.toLowerCase());
      deletedRecord.put("nyplType", bibOrItem);
      deletedRecord.put("deletedDate", deleteDateFormat.format(new Date()));
      deletedRecord.put("deleted", true);
      deletedRecord.put("fixedFields", new HashMap<>());
      deletedRecord.put("varFields", new ArrayList<>());

      if (bibOrItem == Constants.BIB) {
        deletedRecord.put("id", record.get(OWNING_INSTITUTION_BIB_ID));
        logger.info(
            "deleting the bib " + owningInstitution + "-" + record.get(OWNING_INSTITUTION_BIB_ID));
      } else {
        logger.info("deleting the individual item: " + owningInstitution + "-"
            + record.get(OWNING_INSTITUTION_ITEM_ID));
        deletedRecord.put("id", record.get(OWNING_INSTITUTION_ITEM_ID));
        deletedRecord.put("bibIds", new ArrayList<>());
      }

      return new ObjectMapper().writeValueAsString(deletedRecord);
    } catch (JsonGenerationException e) {
      String errMessage =
          "Json Generation Exception while transforming to delete json format - " + e.getMessage();
      logger.error(errMessage + " ", e);
      throw new RecapHarvesterException(errMessage);
    } catch (JsonMappingException e) {
      String errMessage =
          "Json Mapping Exception while transforming to delete json format - " + e.getMessage();
      logger.error(errMessage + " ", e);
      throw new RecapHarvesterException(errMessage);
    } catch (IOException e) {
      String errMessage =
          "IOException while transforming to delete json format - " + e.getMessage();
      logger.error(errMessage + " ", e);
      throw new RecapHarvesterException(errMessage);
    }
  }

}
