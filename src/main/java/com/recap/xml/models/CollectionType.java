
package com.recap.xml.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Java class for collectionType complex type.
 * <p/>
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * 
 * <pre>
 * &lt;complexType name="collectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}record"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{}idDataType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "collection", propOrder = {"record"})
@XmlRootElement(name = "collection")
public class CollectionType {

  private static final Logger LOG = LoggerFactory.getLogger(CollectionType.class);

  @XmlElement(name = "record", nillable = true, namespace = "http://www.loc.gov/MARC21/slim")
  protected List<RecordType> record;
  @XmlAttribute(name = "id")
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  @XmlID
  protected String id;

  /**
   * Gets the value of the record property.
   * <p/>
   * <p/>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the record property.
   * <p/>
   * <p/>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getRecord().add(newItem);
   * </pre>
   * <p/>
   * <p/>
   * <p/>
   * Objects of the following type(s) are allowed in the list {@link RecordType }
   */
  public List<RecordType> getRecord() {
    if (record == null) {
      record = new ArrayList<RecordType>();
    }
    return this.record;
  }

  public void setRecord(List<RecordType> record) {
    this.record = record;
  }

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link String }
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is {@link String }
   */
  public void setId(String value) {
    this.id = value;
  }

}
