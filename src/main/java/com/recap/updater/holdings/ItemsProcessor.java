package com.recap.updater.holdings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.recap.models.Subfield;
import com.recap.models.VarField;
import com.recap.updater.bib.BibConstants;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.Holding;
import com.recap.xml.models.Items;
import com.recap.xml.models.RecordType;
import com.recap.xml.models.SubfieldatafieldType;

public class ItemsProcessor implements Processor {

  private static Logger logger = LoggerFactory.getLogger(ItemsProcessor.class);

  @Override
  public void process(Exchange exchange) {
    Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
    List<Holding> listHolding = (List<Holding>) exchangeContents.get(Constants.LIST_HOLDING);
    Bib bib = (Bib) exchangeContents.get(Constants.BIB);
    List<Item> items = getListItems(listHolding, bib);
    exchangeContents.put(Constants.LIST_ITEMS, items);
    exchange.getIn().setBody(items);
    logger.info("Processing items for bib - " + bib.getId());
  }

  public List<Item> getListItems(List<Holding> listHolding, Bib bib) {
    List<Item> items = new ArrayList<>();
    for (Holding holding : listHolding) {
      List<VarField> holdingVarFields = new ArrayList<>();
      List<RecordType> holdingRecords = holding.getContent().getCollection().getRecord();
      Map<String, String> location = null;
      String callNumber = null;
      for (RecordType holdingRecord : holdingRecords) {
        List<DataFieldType> varFields = holdingRecord.getDatafield();
        for (DataFieldType varField : varFields) {
          if (varField.getTag().equals(ItemConstants.DATAFIELD_TAG_852)) {
            location = getLocation(varField, bib);
            callNumber = getCallNumber(varField);
          }
          VarField varFieldObj = new VarFieldsProcessor().getVarFieldFromRecapDataField(varField);
          holdingVarFields.add(varFieldObj);
        }
      }
      List<Items> recapItems = holding.getItems();
      for (Item item : getItems(recapItems, bib, holdingVarFields, location, callNumber)) {
        items.add(item);
      }
    }
    return items;
  }

  public Map<String, String> getLocation(DataFieldType varField, Bib bib) {
    Map<String, String> location = new LinkedHashMap<>();
    for (SubfieldatafieldType subField : varField.getSubfield()) {
      if (subField.getCode().equals(ItemConstants.SUBFIELD_CODE_b)) {
        location.put(ItemConstants.CODE, subField.getValue());
        String institutionId = bib.getNyplSource().substring(bib.getNyplSource().indexOf('-') + 1);
        location.put(ItemConstants.NAME, ItemConstants.OFFSITE_REQ_IN_ADV + " - " + institutionId);
      }
    }
    return location;
  }

  public String getCallNumber(DataFieldType varField) {
    for (SubfieldatafieldType subField : varField.getSubfield()) {
      if (subField.getCode().equals(ItemConstants.SUBFIELD_CODE_h))
        return subField.getValue();
    }
    return null;
  }

  public List<Item> getItems(List<Items> recapItems, Bib bib, List<VarField> holdingVarFields,
      Map<String, String> location, String callNumber) {
    List<Item> items = new ArrayList<>();
    for (Items recapItem : recapItems) {
      List<RecordType> itemRecords = recapItem.getContent().getCollection().getRecord();
      for (RecordType itemRecord : itemRecords) {
        Item item = new Item();
        List<VarField> varFieldObjects = new ArrayList<>();
        List<DataFieldType> varFields = itemRecord.getDatafield();
        String barcode = null, copyNumber = null, itemType = null, recordNumber = null;
        Map<String, String> itemStatus = null;
        for (DataFieldType varField : varFields) {
          VarField varFieldObj = new VarFieldsProcessor().getVarFieldFromRecapDataField(varField);
          varFieldObjects.add(varFieldObj);
          if (varFieldObj.getMarcTag().equals(ItemConstants.DATAFIELD_TAG_876)) {
            item = setItemId(item, varFieldObj);
            recordNumber = item.getId();
            item = setStatus(item, varFieldObj);
            itemStatus = item.getStatus();
            barcode = getBarcode(varFieldObj);
            item.setBarcode(barcode);
            copyNumber = getCopyNumber(varFieldObj);
            itemType = getItemType(varFieldObj);
            String volPartYearInfo = getVolPartYearInfo(varFieldObj);
            VarField varField949 = new VarFieldsProcessor().getVarField949(varField, callNumber,
                copyNumber, barcode, volPartYearInfo, bib, itemType, item.getStatus(), itemType);
            VarField varFieldBarcode = new VarFieldsProcessor().getVarFieldForBarcode(barcode);
            VarField varFieldVolPartYearInfo =
                new VarFieldsProcessor().getVarFieldForVolPartYear(volPartYearInfo);
            varFieldObjects.add(varField949);
            varFieldObjects.add(varFieldBarcode);
            varFieldObjects.add(varFieldVolPartYearInfo);
          }
        }
        for (VarField holdingVarfield : holdingVarFields) {
          if (!holdingVarfield.getMarcTag().equals(ItemConstants.DATAFIELD_TAG_866))
            varFieldObjects.add(holdingVarfield);
        }
        item.setBibIds(Arrays.asList(bib.getId()));
        item.setNyplSource(bib.getNyplSource());
        item.setNyplType(ItemConstants.ITEM);
        DateFormat dateFormat = new SimpleDateFormat(ItemConstants.SIMPLE_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(ItemConstants.TIMEZONE_GMT));
        String updatedDate = dateFormat.format(new Date());
        item.setUpdatedDate(updatedDate);
        item.setDeleted(false);
        item.setLocation(location);
        item.setCallNumber(callNumber);
        item.setVarFields(varFieldObjects);
        item.setFixedFields(
            getFixedFields(copyNumber, bib, itemType, recordNumber, updatedDate, itemStatus));
        items.add(item);
      }
    }
    return items;
  }

  public String getVolPartYearInfo(VarField varFieldObj) {
    for (Subfield subField : varFieldObj.getSubfields()) {
      if (subField.getTag().equals(ItemConstants.SUBFIELD_CODE_3)) {
        return subField.getContent();
      }
    }
    return null;
  }

  public Item setItemId(Item item, VarField varFieldObj) {
    for (Subfield subField : varFieldObj.getSubfields()) {
      if (subField.getTag().equals(ItemConstants.SUBFIELD_CODE_a)) {
        String id;
        if ((subField.getContent()).startsWith("."))
          id = (subField.getContent()).substring(2, (subField.getContent()).length() - 1);
        else
          id = subField.getContent();
        item.setId(id);
      }
    }
    return item;
  }

  public Item setStatus(Item item, VarField varFieldObj) {
    for (Subfield subField : varFieldObj.getSubfields()) {
      if (subField.getTag().equals(ItemConstants.SUBFIELD_CODE_j)) {
        Map<String, String> status = new LinkedHashMap<>();
        if (subField.getContent().equals(ItemConstants.AVAILABLE)) {
          status.put(ItemConstants.CODE, ItemConstants.AVAILABILITY_CODE_VAL);
          status.put(ItemConstants.DISPLAY, ItemConstants.AVAILABILITY_DISPLAY);
        } else if (subField.getContent().equals(ItemConstants.NOT_AVAILABLE)) {
          status.put(ItemConstants.CODE, ItemConstants.UNAVAILABLE_CODE);
          status.put(ItemConstants.DISPLAY, ItemConstants.UNAVAILABLE_DISPLAY);
        }
        item.setStatus(status);
      }
    }
    return item;
  }

  public String getBarcode(VarField varFieldObj) {
    for (Subfield subField : varFieldObj.getSubfields()) {
      if (subField.getTag().equals(ItemConstants.SUBFIELD_CODE_p)) {
        return (subField.getContent());
      }
    }
    return null;
  }

  public String getCopyNumber(VarField varFieldObj) {
    for (Subfield subField : varFieldObj.getSubfields()) {
      if (subField.getTag().equals(ItemConstants.SUBFIELD_CODE_t)) {
        return subField.getContent();
      }
    }
    return null;
  }

  public String getItemType(VarField varFieldObj) {
    for (Subfield subField : varFieldObj.getSubfields()) {
      if (subField.getTag().equals(ItemConstants.SUBFIELD_CODE_h)) {
        return subField.getContent();
      }
    }
    return null;
  }

  public Map<String, Map<String, Object>> getFixedFields(String copyNumber, Bib bib,
      String itemTypeFromRecap, String recordNumber, String updatedDate,
      Map<String, String> itemStatus) {
    Map<String, Map<String, Object>> fixedFields = new HashMap<>();
    fixedFields.put(ItemConstants.FIXED_FIELDS_57,
        new FixedFieldsProcessor().getFixedFieldsBibHold());
    fixedFields.put(ItemConstants.FIXED_FIELDS_58,
        new FixedFieldsProcessor().getFixedFieldsCopyNumber(copyNumber));
    fixedFields.put(ItemConstants.FIXED_FIELDS_59,
        new FixedFieldsProcessor().getFixedFieldsItemCode1());
    fixedFields.put(ItemConstants.FIXED_FIELDS_60,
        new FixedFieldsProcessor().getFixedFieldsItemCode2());
    fixedFields.put(ItemConstants.FIXED_FIELDS_61,
        new FixedFieldsProcessor().getFixedFieldsItemType(bib, itemTypeFromRecap));
    fixedFields.put(ItemConstants.FIXED_FIELDS_62,
        new FixedFieldsProcessor().getFixedFieldsPrice());
    fixedFields.put(ItemConstants.FIXED_FIELDS_64,
        new FixedFieldsProcessor().getFixedFieldsCheckoutLocation());
    fixedFields.put(ItemConstants.FIXED_FIELDS_68,
        new FixedFieldsProcessor().getFixedFieldsLastCheckin());
    fixedFields.put(ItemConstants.FIXED_FIELDS_70,
        new FixedFieldsProcessor().getFixedFieldsCheckinLocation());
    fixedFields.put(ItemConstants.FIXED_FIELDS_74,
        new FixedFieldsProcessor().getFixedFieldsItemUse3());
    fixedFields.put(ItemConstants.FIXED_FIELDS_76,
        new FixedFieldsProcessor().getFixedFieldsTotalCheckouts());
    fixedFields.put(ItemConstants.FIXED_FIELDS_77,
        new FixedFieldsProcessor().getFixedFieldsTotalRenewals());
    fixedFields.put(ItemConstants.FIXED_FIELDS_79,
        new FixedFieldsProcessor().getFixedFieldsLocation(bib));
    fixedFields.put(ItemConstants.FIXED_FIELDS_80,
        new FixedFieldsProcessor().getFixedFieldsRecordType());
    fixedFields.put(ItemConstants.FIXED_FIELDS_81,
        new FixedFieldsProcessor().getFixedFieldsRecordNumber(recordNumber));
    fixedFields.put(ItemConstants.FIXED_FIELDS_83,
        new FixedFieldsProcessor().getFixedFieldsCreatedDate());
    fixedFields.put(ItemConstants.FIXED_FIELDS_84,
        new FixedFieldsProcessor().getFixedFieldsUpdatedDate(updatedDate));
    fixedFields.put(ItemConstants.FIXED_FIELDS_85,
        new FixedFieldsProcessor().getFixedFieldsNumberRevisions());
    fixedFields.put(ItemConstants.FIXED_FIELDS_86,
        new FixedFieldsProcessor().getFixedFieldsAgency());
    fixedFields.put(ItemConstants.FIXED_FIELDS_88,
        new FixedFieldsProcessor().getFixedFieldsStatus(itemStatus));
    fixedFields.put(ItemConstants.FIXED_FIELDS_93,
        new FixedFieldsProcessor().getFixedFieldsInternalUse());
    fixedFields.put(ItemConstants.FIXED_FIELDS_94,
        new FixedFieldsProcessor().getFixedFieldsCopyUse());
    fixedFields.put(ItemConstants.FIXED_FIELDS_97,
        new FixedFieldsProcessor().getFixedFieldsItemMessage());
    fixedFields.put(ItemConstants.FIXED_FIELDS_98,
        new FixedFieldsProcessor().getFixedFieldsPdate());
    fixedFields.put(ItemConstants.FIXED_FIELDS_108,
        new FixedFieldsProcessor().getFixedFieldsOPACMessage(itemTypeFromRecap));
    fixedFields.put(ItemConstants.FIXED_FIELDS_109,
        new FixedFieldsProcessor().getFixedFieldsYearToDateCirc());
    fixedFields.put(ItemConstants.FIXED_FIELDS_110,
        new FixedFieldsProcessor().getFixedFieldsLastYearCirc());
    fixedFields.put(ItemConstants.FIXED_FIELDS_127,
        new FixedFieldsProcessor().getFixedFieldsItemAgency());
    fixedFields.put(ItemConstants.FIXED_FIELDS_161,
        new FixedFieldsProcessor().getFixedFieldsVICentral());
    fixedFields.put(ItemConstants.FIXED_FIELDS_162,
        new FixedFieldsProcessor().getFixedFieldsIRDistLearnSameSite());
    fixedFields.put(ItemConstants.FIXED_FIELDS_264,
        new FixedFieldsProcessor().getFixedFieldsHoldingsItemTag());
    fixedFields.put(ItemConstants.FIXED_FIELDS_265,
        new FixedFieldsProcessor().getFixedFieldsInheritLocation());
    return fixedFields;
  }

  public Map<String, Object> processItemTypeFromRecapItemType(Bib bib, String itemTypeFromRecap) {
    Map<String, Object> itemTypeLabelVals = new LinkedHashMap<>();
    if (bib.getBibLevel().get(BibConstants.VALUE).equals(BibConstants.MONOGRAPH)
        && itemTypeFromRecap.trim().toLowerCase()
            .equals(ItemConstants.IN_LIBRARY_USE.trim().toLowerCase())) {
      itemTypeLabelVals.put(ItemConstants.LABEL, ItemConstants.ITEM_TYPE);
      itemTypeLabelVals.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_2);
      return itemTypeLabelVals;
    } else if (bib.getBibLevel().get(BibConstants.VALUE).equals(BibConstants.MONOGRAPH)
        && itemTypeFromRecap.trim().toLowerCase()
            .equals(ItemConstants.SUPERVISED_USE.trim().toLowerCase())) {
      itemTypeLabelVals.put(ItemConstants.LABEL, ItemConstants.ITEM_TYPE);
      itemTypeLabelVals.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_66);
      return itemTypeLabelVals;
    } else if (bib.getBibLevel().get(BibConstants.VALUE).equals(BibConstants.MONOGRAPH)
        && (itemTypeFromRecap.equals(" ") || (itemTypeFromRecap.equals("")))) {
      itemTypeLabelVals.put(ItemConstants.LABEL, ItemConstants.ITEM_TYPE);
      itemTypeLabelVals.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_55);
      return itemTypeLabelVals;
    } else if (bib.getBibLevel().get(BibConstants.VALUE).equals(BibConstants.SERIAL)) {
      itemTypeLabelVals.put(ItemConstants.LABEL, ItemConstants.ITEM_TYPE);
      itemTypeLabelVals.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_3);
      return itemTypeLabelVals;
    } else if (bib.getBibLevel().get(BibConstants.VALUE).equals(BibConstants.MICROFORM)) {
      itemTypeLabelVals.put(ItemConstants.LABEL, ItemConstants.ITEM_TYPE);
      itemTypeLabelVals.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_6);
      return itemTypeLabelVals;
    } else {
      itemTypeLabelVals.put(ItemConstants.LABEL, ItemConstants.ITEM_TYPE);
      itemTypeLabelVals.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_2);
      return itemTypeLabelVals;
    }
  }

}
