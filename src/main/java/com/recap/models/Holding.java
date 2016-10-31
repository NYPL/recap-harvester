package com.recap.models;

import java.util.List;

import com.recap.xml.models.ContentType;
import com.recap.xml.models.Items;

public class Holding {
	
	private ContentType contentType;
	
	private List<Items> items;
	
	private String owningInstitutionHoldingsId;

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public List<Items> getItems() {
		return items;
	}

	public void setItems(List<Items> items) {
		this.items = items;
	}

	public String getOwningInstitutionHoldingsId() {
		return owningInstitutionHoldingsId;
	}

	public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
		this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
	}

}
