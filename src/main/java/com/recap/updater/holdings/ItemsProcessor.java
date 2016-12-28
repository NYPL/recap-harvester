package com.recap.updater.holdings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.models.Item;
import com.recap.models.SubField;
import com.recap.models.VarField;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.Holding;
import com.recap.xml.models.Items;
import com.recap.xml.models.RecordType;
import com.recap.xml.models.SubfieldatafieldType;

public class ItemsProcessor implements Processor{
	
	private static Logger logger = LoggerFactory.getLogger(ItemsProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		List<Holding> listHolding = (List<Holding>) exchangeContents.get(Constants.LIST_HOLDING);
		Bib bib = (Bib) exchangeContents.get(Constants.BIB);
		List<Item> items = getListItems(listHolding, bib);
		exchangeContents.put(Constants.LIST_ITEMS, items);
		exchange.getIn().setBody(exchangeContents);
		logger.info("Processing items for bib - " + bib.getId());
	}
	
	public List<Item> getListItems(List<Holding> listHolding, Bib bib){
		List<Item> items = new ArrayList<>();
		for(Holding holding : listHolding){
			List<VarField> holdingVarFields = new ArrayList<>();
			List<RecordType> holdingRecords = holding.getContent().getCollection().getRecord();
			for(RecordType holdingRecord : holdingRecords){
				List<DataFieldType> varFields = holdingRecord.getDatafield();
				for(DataFieldType varField : varFields){
					VarField varFieldObj = getVarFieldFromRecapDataField(varField);
					holdingVarFields.add(varFieldObj);
				}
			}
			List<Items> recapItems = holding.getItems();
			for(Items recapItem : recapItems){
				List<RecordType> itemRecords = recapItem.getContent().getCollection().getRecord();
				for(RecordType itemRecord : itemRecords){
					Item item = new Item();
					List<VarField> varFieldObjects = new ArrayList<>();
					List<DataFieldType> varFields = itemRecord.getDatafield();
					for(DataFieldType varField : varFields){
						VarField varFieldObj = getVarFieldFromRecapDataField(varField);
						if(varFieldObj.getMarcTag().equals("876")){
							for(SubField subField : varFieldObj.getSubFields()){
								if(subField.getTag().equals("a")){
									String id;
									if(subField.getContent().startsWith("."))
										id = subField.getContent().substring(2, subField.getContent()
												.length()-1);
									else
										id = subField.getContent();				
									item.setId(id);
								}
							}
						}
						varFieldObjects.add(varFieldObj);
					}
					for(VarField holdingVarfield : holdingVarFields){
						if(!holdingVarfield.getMarcTag().equals("866"))
							varFieldObjects.add(holdingVarfield);
					}
					item.setBibIds(Arrays.asList(bib.getId()));
					item.setNyplSource(bib.getNyplSource());
					item.setNyplType("item");
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
					item.setUpdatedDate(dateFormat.format(new Date()));
					item.setVarFields(varFieldObjects);
					items.add(item);
				}
			}
		}
		return items;
	}
	
	
	public VarField getVarFieldFromRecapDataField(DataFieldType dataField){
		VarField varFieldObj = new VarField();
		varFieldObj.setFieldTag(dataField.getId());
		varFieldObj.setInd1(dataField.getInd1());
		varFieldObj.setInd2(dataField.getInd2());
		varFieldObj.setMarcTag(dataField.getTag());
		List<SubfieldatafieldType> subFields = dataField.getSubfield();
		List<SubField> subFieldObjects = new ArrayList<>();
		for(SubfieldatafieldType subField : subFields){
			SubField subFieldObj = new SubField();
			subFieldObj.setContent(subField.getValue());
			subFieldObj.setTag(subField.getCode());
			subFieldObjects.add(subFieldObj);
		}
		varFieldObj.setSubFields(subFieldObjects);
		return varFieldObj;
	}

}
