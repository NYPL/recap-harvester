package com.recap.updater.holdings;

import java.util.LinkedHashMap;
import java.util.Map;

import com.recap.models.Bib;
import com.recap.updater.bib.BibConstants;

public class FixedFieldsProcessor {

  public Map<String, Object> getFixedFieldsBibHold() {
    Map<String, Object> fixedFieldsBibHold = new LinkedHashMap<>();
    fixedFieldsBibHold.put(ItemConstants.LABEL, ItemConstants.BIB_HOLD);
    fixedFieldsBibHold.put(ItemConstants.VALUE, false);
    return fixedFieldsBibHold;
  }

  public Map<String, Object> getFixedFieldsCopyNumber(String copyNumber) {
    Map<String, Object> fixedFieldsCopyNumber = new LinkedHashMap<>();
    fixedFieldsCopyNumber.put(ItemConstants.LABEL, ItemConstants.COPY_NUMBER);
    fixedFieldsCopyNumber.put(ItemConstants.VALUE, copyNumber);
    return fixedFieldsCopyNumber;
  }

  public Map<String, Object> getFixedFieldsItemCode1() {
    Map<String, Object> fixedFieldsItemCode1 = new LinkedHashMap<>();
    fixedFieldsItemCode1.put(ItemConstants.LABEL, ItemConstants.ITEM_CODE_1);
    fixedFieldsItemCode1.put(ItemConstants.VALUE, null);
    return fixedFieldsItemCode1;
  }

  public Map<String, Object> getFixedFieldsItemCode2() {
    Map<String, Object> fixedFieldsItemCode1 = new LinkedHashMap<>();
    fixedFieldsItemCode1.put(ItemConstants.LABEL, ItemConstants.ITEM_CODE_2);
    fixedFieldsItemCode1.put(ItemConstants.VALUE, null);
    return fixedFieldsItemCode1;
  }

  public Map<String, Object> getFixedFieldsItemType(Bib bib, String itemTypeFromRecap) {
    return new ItemsProcessor().processItemTypeFromRecapItemType(bib, itemTypeFromRecap);
  }

  public Map<String, Object> getFixedFieldsPrice() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.PRICE);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsCheckoutLocation() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.CHECKOUT_LOCATION);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsLastCheckin() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.LAST_CHECKIN);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsCheckinLocation() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.CHECKIN_LOCATION);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsItemUse3() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.ITEM_USE_3);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsTotalCheckouts() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.TOTAL_CHECKOUTS);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsTotalRenewals() {
    Map<String, Object> fixedFieldsPrice = new LinkedHashMap<>();
    fixedFieldsPrice.put(ItemConstants.LABEL, ItemConstants.TOTAL_RENEWALS);
    fixedFieldsPrice.put(ItemConstants.VALUE, null);
    return fixedFieldsPrice;
  }

  public Map<String, Object> getFixedFieldsLocation(Bib bib) {
    Map<String, Object> fixedFieldsLocation = new LinkedHashMap<>();
    fixedFieldsLocation.put(ItemConstants.LABEL, ItemConstants.LOCATION);
    StringBuffer locationValue = new StringBuffer();
    locationValue.append(ItemConstants.RECAP_INITIALS);
    locationValue.append(bib.getNyplSource().substring(bib.getNyplSource().indexOf('-') + 1));
    fixedFieldsLocation.put(ItemConstants.VALUE, locationValue.toString().toLowerCase());
    fixedFieldsLocation.put(ItemConstants.DISPLAY, ItemConstants.OFFSITE_REQ_IN_ADV);
    return fixedFieldsLocation;
  }

  public Map<String, Object> getFixedFieldsRecordType() {
    Map<String, Object> fixedFieldsRecordType = new LinkedHashMap<>();
    fixedFieldsRecordType.put(ItemConstants.LABEL, ItemConstants.RECORD_TYPE);
    fixedFieldsRecordType.put(ItemConstants.VALUE, ItemConstants.RECORD_TYPE_ITEM);
    return fixedFieldsRecordType;
  }

  public Map<String, Object> getFixedFieldsRecordNumber(String recordNumber) {
    Map<String, Object> fixedFieldsRecordType = new LinkedHashMap<>();
    fixedFieldsRecordType.put(ItemConstants.LABEL, ItemConstants.RECORD_NUMBER);
    fixedFieldsRecordType.put(ItemConstants.VALUE, recordNumber);
    return fixedFieldsRecordType;
  }

  public Map<String, Object> getFixedFieldsCreatedDate() {
    Map<String, Object> fixedFieldsCreatedDate = new LinkedHashMap<>();
    fixedFieldsCreatedDate.put(ItemConstants.LABEL, ItemConstants.CREATED_DATE);
    fixedFieldsCreatedDate.put(ItemConstants.VALUE, null);
    return fixedFieldsCreatedDate;
  }

  public Map<String, Object> getFixedFieldsUpdatedDate(String updatedDate) {
    Map<String, Object> fixedFieldsUpdatedDate = new LinkedHashMap<>();
    fixedFieldsUpdatedDate.put(ItemConstants.LABEL, ItemConstants.UPDATED_DATE);
    fixedFieldsUpdatedDate.put(ItemConstants.VALUE, updatedDate);
    return fixedFieldsUpdatedDate;
  }

  public Map<String, Object> getFixedFieldsNumberRevisions() {
    Map<String, Object> fixedFieldsNumOfRevisions = new LinkedHashMap<>();
    fixedFieldsNumOfRevisions.put(ItemConstants.LABEL, ItemConstants.NUMBER_OF_REVISIONS);
    fixedFieldsNumOfRevisions.put(ItemConstants.VALUE, null);
    return fixedFieldsNumOfRevisions;
  }

  public Map<String, Object> getFixedFieldsAgency() {
    Map<String, Object> fixedFieldsAgency = new LinkedHashMap<>();
    fixedFieldsAgency.put(ItemConstants.LABEL, ItemConstants.AGENCY);
    fixedFieldsAgency.put(ItemConstants.VALUE, ItemConstants.AGENCY_209);
    return fixedFieldsAgency;
  }

  public Map<String, Object> getFixedFieldsStatus(Map<String, String> itemStatus) {
    Map<String, Object> fixedFieldsStatus = new LinkedHashMap<>();
    fixedFieldsStatus.put(ItemConstants.LABEL, ItemConstants.STATUS);
    fixedFieldsStatus.put(ItemConstants.VALUE, itemStatus.get(ItemConstants.CODE));
    fixedFieldsStatus.put(ItemConstants.DISPLAY, itemStatus.get(ItemConstants.DISPLAY));
    return fixedFieldsStatus;
  }

  public Map<String, Object> getFixedFieldsInternalUse() {
    Map<String, Object> fixedFieldsInternalUse = new LinkedHashMap<>();
    fixedFieldsInternalUse.put(ItemConstants.LABEL, ItemConstants.INTERNAL_USE);
    fixedFieldsInternalUse.put(ItemConstants.VALUE, null);
    return fixedFieldsInternalUse;
  }

  public Map<String, Object> getFixedFieldsCopyUse() {
    Map<String, Object> fixedFieldsCopyUse = new LinkedHashMap<>();
    fixedFieldsCopyUse.put(ItemConstants.LABEL, ItemConstants.COPY_USE);
    fixedFieldsCopyUse.put(ItemConstants.VALUE, null);
    return fixedFieldsCopyUse;
  }

  public Map<String, Object> getFixedFieldsItemMessage() {
    Map<String, Object> fixedFieldsItemMessage = new LinkedHashMap<>();
    fixedFieldsItemMessage.put(ItemConstants.LABEL, ItemConstants.ITEM_MESSAGE);
    fixedFieldsItemMessage.put(ItemConstants.VALUE, null);
    return fixedFieldsItemMessage;
  }

  public Map<String, Object> getFixedFieldsPdate() {
    Map<String, Object> fixedFieldsPdate = new LinkedHashMap<>();
    fixedFieldsPdate.put(ItemConstants.LABEL, ItemConstants.PDATE);
    fixedFieldsPdate.put(ItemConstants.VALUE, null);
    return fixedFieldsPdate;
  }

  public Map<String, Object> getFixedFieldsOPACMessage(String itemTypeReCAP) {
    Map<String, Object> fixedFieldsOPACMessage = new LinkedHashMap<>();
    if (itemTypeReCAP.trim().equals(ItemConstants.IN_LIBRARY_USE.trim()) || itemTypeReCAP.equals("")
        || itemTypeReCAP.equals(" ")) {
      fixedFieldsOPACMessage.put(ItemConstants.LABEL, ItemConstants.OPAC_MESSAGE);
      fixedFieldsOPACMessage.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_2);
    } else if (itemTypeReCAP.trim().equals(ItemConstants.SUPERVISED_USE)) {
      fixedFieldsOPACMessage.put(ItemConstants.LABEL, ItemConstants.OPAC_MESSAGE);
      fixedFieldsOPACMessage.put(ItemConstants.VALUE, ItemConstants.ITEM_TYPE_VAL_U);
    }
    return fixedFieldsOPACMessage;
  }

  public Map<String, Object> getFixedFieldsYearToDateCirc() {
    Map<String, Object> fixedFieldsYearToDateCirc = new LinkedHashMap<>();
    fixedFieldsYearToDateCirc.put(ItemConstants.LABEL, ItemConstants.YEAR_TO_DATE_CIRC);
    fixedFieldsYearToDateCirc.put(ItemConstants.VALUE, null);
    return fixedFieldsYearToDateCirc;
  }

  public Map<String, Object> getFixedFieldsLastYearCirc() {
    Map<String, Object> fixedFieldsLastYearCirc = new LinkedHashMap<>();
    fixedFieldsLastYearCirc.put(ItemConstants.LABEL, ItemConstants.LAST_YEAR_CIRC);
    fixedFieldsLastYearCirc.put(ItemConstants.VALUE, null);
    return fixedFieldsLastYearCirc;
  }

  public Map<String, Object> getFixedFieldsItemAgency() {
    Map<String, Object> fixedFieldsItemAgency = new LinkedHashMap<>();
    fixedFieldsItemAgency.put(ItemConstants.LABEL, ItemConstants.ITEM_AGENCY);
    fixedFieldsItemAgency.put(ItemConstants.VALUE, ItemConstants.AGENCY_209);
    fixedFieldsItemAgency.put(ItemConstants.DISPLAY, ItemConstants.ITEM_AGENCY_RECAP_DEFAULT);
    return fixedFieldsItemAgency;
  }

  public Map<String, Object> getFixedFieldsVICentral() {
    Map<String, Object> fixedFieldsVICentral = new LinkedHashMap<>();
    fixedFieldsVICentral.put(ItemConstants.LABEL, ItemConstants.VI_CENTRAL);
    fixedFieldsVICentral.put(ItemConstants.VALUE, null);
    return fixedFieldsVICentral;
  }

  public Map<String, Object> getFixedFieldsIRDistLearnSameSite() {
    Map<String, Object> fixedFieldsIRDistLearnSameSite = new LinkedHashMap<>();
    fixedFieldsIRDistLearnSameSite.put(ItemConstants.LABEL, ItemConstants.IR_DIST_LEARN_SAME_SITE);
    fixedFieldsIRDistLearnSameSite.put(ItemConstants.VALUE, null);
    return fixedFieldsIRDistLearnSameSite;
  }

  public Map<String, Object> getFixedFieldsHoldingsItemTag() {
    Map<String, Object> fixedFieldsHoldingsItemTag = new LinkedHashMap<>();
    fixedFieldsHoldingsItemTag.put(ItemConstants.LABEL, ItemConstants.HOLDINGS_ITEM_TAG);
    fixedFieldsHoldingsItemTag.put(ItemConstants.VALUE, null);
    return fixedFieldsHoldingsItemTag;
  }

  public Map<String, Object> getFixedFieldsInheritLocation() {
    Map<String, Object> fixedFieldsInheritLocation = new LinkedHashMap<>();
    fixedFieldsInheritLocation.put(ItemConstants.LABEL, ItemConstants.INHERIT_LOCATION);
    fixedFieldsInheritLocation.put(ItemConstants.VALUE, null);
    return fixedFieldsInheritLocation;
  }
}
