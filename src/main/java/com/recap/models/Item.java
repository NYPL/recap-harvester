package com.recap.models;

import java.util.List;

public class Item {
	
	private String id;
	private String nyplSource;
	private String nyplType;
	private List<String> bibIds;
	private String updatedDate;
	private String createdDate;
	private List<VarField> varFields;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNyplSource() {
		return nyplSource;
	}

	public void setNyplSource(String nyplSource) {
		this.nyplSource = nyplSource;
	}

	public String getNyplType() {
		return nyplType;
	}

	public void setNyplType(String nyplType) {
		this.nyplType = nyplType;
	}

	public List<String> getBibIds() {
		return bibIds;
	}

	public void setBibIds(List<String> bibIds) {
		this.bibIds = bibIds;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public List<VarField> getVarFields() {
		return varFields;
	}

	public void setVarFields(List<VarField> varFields) {
		this.varFields = varFields;
	}
}
