
package com.recap.xml.models;

/**
 * <p>Java class for recordTypeType.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;simpleType name="recordTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="Bibliographic"/>
 *     &lt;enumeration value="Authority"/>
 *     &lt;enumeration value="Holdings"/>
 *     &lt;enumeration value="Classification"/>
 *     &lt;enumeration value="Community"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */

public enum RecordTypeType {


    BIBLIOGRAPHIC("Bibliographic"),
   
    AUTHORITY("Authority"),
    
    HOLDINGS("Holdings"),
    
    CLASSIFICATION("Classification"),
   
    COMMUNITY("Community");
    private final String value;

    RecordTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RecordTypeType fromValue(String v) {
        for (RecordTypeType c : RecordTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
