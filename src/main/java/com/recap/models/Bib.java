package com.recap.models;

import java.util.List;
import java.util.Map;

public class Bib {
	
	private String id;
	private String nyplSource;
	private String updatedDate;
	private String createdDate;
	private Boolean deleted;
	private Boolean suppressed;
	private String lang;
	private String title;
	private String author;
	private Map<String, String> materialType;
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getSuppressed() {
		return suppressed;
	}

	public void setSuppressed(Boolean suppressed) {
		this.suppressed = suppressed;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Map<String, String> getMaterialType() {
		return materialType;
	}

	public void setMaterialType(Map<String, String> materialType) {
		this.materialType = materialType;
	}

	public List<VarField> getVarFields() {
		return varFields;
	}

	public void setVarFields(List<VarField> varFields) {
		this.varFields = varFields;
	}	

}
