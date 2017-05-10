package com.recap.updater.holdings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.recap.models.Bib;
import com.recap.models.SubField;
import com.recap.models.VarField;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.SubfieldatafieldType;

public class VarFieldsProcessor {

  public VarField getVarFieldFromRecapDataField(DataFieldType dataField) {
    VarField varFieldObj = new VarField();
    varFieldObj.setFieldTag(dataField.getId());
    varFieldObj.setInd1(dataField.getInd1());
    varFieldObj.setInd2(dataField.getInd2());
    varFieldObj.setMarcTag(dataField.getTag());
    List<SubfieldatafieldType> subFields = dataField.getSubfield();
    List<SubField> subFieldObjects = new ArrayList<>();
    for (SubfieldatafieldType subField : subFields) {
      SubField subFieldObj = new SubField();
      subFieldObj.setContent(subField.getValue());
      subFieldObj.setTag(subField.getCode());
      subFieldObjects.add(subFieldObj);
    }
    varFieldObj.setSubFields(subFieldObjects);
    return varFieldObj;
  }

  public VarField getVarField949(DataFieldType dataField, String callNumber, String copyNumber,
      String barcode, String volPartYearInfo, Bib bib, String itemType,
      Map<String, String> itemStatus, String recapItemType) {
    VarField varFieldObj = new VarField();
    varFieldObj.setFieldTag(ItemConstants.VARFIELD_FIELD_TAG_949);
    varFieldObj.setInd1(" ");
    varFieldObj.setInd2(" ");
    varFieldObj.setMarcTag(ItemConstants.DATAFIELD_TAG_949);
    List<SubField> subFields = new ArrayList<>();

    SubField callNumberSubField = new SubField();
    callNumberSubField.setContent(callNumber);
    callNumberSubField.setTag(ItemConstants.SUBFIELD_CODE_a);
    subFields.add(callNumberSubField);

    SubField volPartYearInfoSubField = new SubField();
    volPartYearInfoSubField.setContent(volPartYearInfo);
    volPartYearInfoSubField.setTag(ItemConstants.SUBFIELD_CODE_c);
    subFields.add(volPartYearInfoSubField);

    SubField subFieldg = new SubField();
    subFieldg.setContent(copyNumber);
    subFieldg.setTag(ItemConstants.SUBFIELD_CODE_g);
    subFields.add(subFieldg);

    SubField subFieldBarcode = new SubField();
    subFieldBarcode.setContent(barcode);
    subFieldBarcode.setTag(ItemConstants.SUBFIELD_CODE_i);
    subFields.add(subFieldBarcode);

    SubField subFieldLocation = new SubField();
    StringBuffer locationValue = new StringBuffer();
    locationValue.append(ItemConstants.RECAP_INITIALS);
    locationValue.append(bib.getNyplSource().substring(bib.getNyplSource().indexOf('-') + 1));
    subFieldLocation.setContent(locationValue.toString().toLowerCase());
    subFieldLocation.setTag(ItemConstants.SUBFIELD_CODE_l);
    subFields.add(subFieldLocation);

    SubField subFieldUseRestriction = new SubField();
    if (itemType.equals(ItemConstants.IN_LIBRARY_USE) || itemType.equals("")
        || itemType.equals(" ")) {
      subFieldUseRestriction.setContent(ItemConstants.ITEM_TYPE_VAL_2);
    } else if (itemType.equals(ItemConstants.SUPERVISED_USE)) {
      subFieldUseRestriction.setContent(ItemConstants.ITEM_TYPE_VAL_U);
    }
    subFieldUseRestriction.setTag(ItemConstants.SUBFIELD_CODE_o);
    subFields.add(subFieldUseRestriction);

    SubField subFieldStatus = new SubField();
    try {
      if (itemStatus != null
          && itemStatus.get(ItemConstants.CODE).equals(ItemConstants.AVAILABILITY_CODE_VAL))
        subFieldStatus.setContent(itemStatus.get(ItemConstants.CODE));
      else if (itemStatus != null
          && itemStatus.get(ItemConstants.CODE).equals(ItemConstants.UNAVAILABLE_CODE))
        subFieldStatus.setContent(itemStatus.get(ItemConstants.CODE));
      subFieldStatus.setTag(ItemConstants.SUBFIELD_CODE_s);
    } catch (NullPointerException npe) {
      // nothing to do
    }
    subFields.add(subFieldStatus);

    SubField subFieldItemType = new SubField();
    Map<String, Object> itemTypeLabelVals =
        new ItemsProcessor().processItemTypeFromRecapItemType(bib, itemType);
    subFieldItemType.setContent((String) itemTypeLabelVals.get(ItemConstants.DISPLAY));
    subFieldItemType.setTag(ItemConstants.SUBFIELD_CODE_t);
    subFields.add(subFieldItemType);

    varFieldObj.setSubFields(subFields);
    return varFieldObj;

  }

  public VarField getVarFieldForBarcode(String barcode) {
    VarField varFieldObj = new VarField();
    varFieldObj.setFieldTag(ItemConstants.VARFIELD_FIELD_TAG_b);
    varFieldObj.setContent(barcode);
    return varFieldObj;
  }

  public VarField getVarFieldForVolPartYear(String volPartYearInfo) {
    VarField varField = new VarField();
    varField.setFieldTag(ItemConstants.VARFIELD_FIELD_TAG_v);
    varField.setContent(volPartYearInfo);
    return varField;
  }

}
