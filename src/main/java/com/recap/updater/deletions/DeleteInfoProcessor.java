package com.recap.updater.deletions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import org.codehaus.jackson.map.ObjectMapper;
import java.util.HashMap;

public class DeleteInfoProcessor implements Processor {

	boolean doCleanUp = false;

	private static Logger logger = LoggerFactory.getLogger(DeleteInfoProcessor.class);

	public DeleteInfoProcessor(boolean doCleanUp) {
		this.doCleanUp = doCleanUp;
	}

	@Override
	public void process(Exchange exchange) throws RecapHarvesterException {
		try {
			File dirJsonFileThatHasDeleteInfo = exchange.getIn().getBody(File.class);

			if (dirJsonFileThatHasDeleteInfo != null) {
				File[] files = dirJsonFileThatHasDeleteInfo.listFiles();

				List<String> deletedBibs = new ArrayList<>();
				List<String> deletedItems = new ArrayList<>();

				for (File file : files) {
					if (file.getName().trim().endsWith(".json")) {
						ObjectMapper mapper = new ObjectMapper();
						List<Map<String, Map<String, String>>> deletedJSON = mapper.readValue(file,
								mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

						for (Map<String, Map<String, String>> bib : deletedJSON) {

							Boolean deleteAllItems = Boolean.valueOf(bib.get("bib").get("deleteAllItems"));

							// DEAR JOBIN: I think the body of the if & else can
							// use the same private
							// method, something like "transformToDeleted()"
							// that accepts the the delete-feed Map of an Item
							// and returns
							// the JSON String (you're expecting the elements of
							// deletedBibs & deletedItems to be JSON strings,
							// right?)
							// rgar will be pushed into
							if (deleteAllItems) {
								// Go to Bib Service and get items for the bib.
								// for each Item response
									// push a JSON string that looks like this to deletedItems:
								// {
								// "id": "10000001",
								// "deletedDate": "2011-02-28",
								// "deleted": true
								// },

								// Does this also need to insert something into
								// deletedBibs?
							} else {
								// iterate through bib.get("bib").get("items")
								// for each item
								// push a JSON string that looks like above to
								// deletedItems
							}

						}
					}
				}

				Map<String, List<String>> bibsAndItemsDeleted = new HashMap<>();
				bibsAndItemsDeleted.put(Constants.DELETED_BIBS, deletedBibs);
				bibsAndItemsDeleted.put(Constants.DELETED_ITEMS, deletedItems);

				if (doCleanUp) {
					// make sure to delete the json files from local directory
					// after processing
				}

				exchange.getIn().setBody(bibsAndItemsDeleted, Map.class);
			}
		} catch (Exception e) {
			logger.error("Error occurred while extracting deleted bibs and items info from json file - ", e);
			throw new RecapHarvesterException("Error while extracting bibs and items info from json file");
		}
	}

}
