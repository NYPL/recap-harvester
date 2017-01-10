package com.recap.updater.bib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.models.Bib;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.RecordType;

@Component
public class BibProcessor implements Processor{
	
	private BaseConfig baseConfig;
	
	private static Logger logger = LoggerFactory.getLogger(BibProcessor.class);
	
	public BibProcessor(BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
		Bib bib = getBibFromBibRecord(bibRecord);

		logger.info("Processing bib - " + bib.getId());
		exchange.getIn().setBody(bib);
	}
	
	public Bib getBibFromBibRecord(BibRecord bibRecord) throws Exception{
		try{
			Bib bib = new Bib();
			String bibId;
			String originalBibIdFromRecap = bibRecord.getBib().getOwningInstitutionBibId();
			if(originalBibIdFromRecap.startsWith(".")){
				bibId = originalBibIdFromRecap.substring(2, originalBibIdFromRecap.length() - 1);
			}else
				bibId = bibRecord.getBib().getOwningInstitutionBibId();
			bib.setId(bibId);
			bib.setNyplSource("recap-" + bibRecord.getBib().getOwningInstitutionId());
			bib.setNyplType("bib");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			bib.setUpdatedDate(dateFormat.format(new Date()));
			bib.setDeleted(false);
			bib.setSuppressed(false);
			List<RecordType> bibRecordType = bibRecord.getBib().getContent().getCollection().
					getRecord();
			if(bibRecordType.size() == 1){
				bib.setTitle(new BibFieldsProcessor(baseConfig).getTitle(bibRecordType));
				bib.setAuthor(new BibFieldsProcessor(baseConfig).getAuthor(bibRecordType));
				bib.setLang(new BibFieldsProcessor(baseConfig).getLanguageField(bibRecordType, bib));
				bib.setMaterialType(new BibFieldsProcessor(baseConfig).getMaterialType(bibRecordType, bib));
				bib.setBibLevel(new BibFieldsProcessor(baseConfig).getBibLevel(bibRecordType, bib));
				bib.setPublishYear(new BibFieldsProcessor(baseConfig).getPublishYear(bibRecordType, bib));
				bib.setCountry(new BibFieldsProcessor(baseConfig).getCountry(bibRecordType, bib));
				bib.setFixedFields(new BibFieldsProcessor(baseConfig).
						getFixedFields(bibRecord, bibRecordType, bib.getUpdatedDate(), bib));
				bib.setVarFields(new BibFieldsProcessor(baseConfig).getVarFields(bibRecordType, bib));
			}
			return bib;
		}catch(Exception e){
			logger.error("Error occurred while setting Bib properties of bib -  " + 
		"recap-" + bibRecord.getBib().getOwningInstitutionId() + ", bibId - " + bibRecord.
		getBib().getOwningInstitutionBibId(), e);
			throw new Exception(e.getMessage());
		}
	}

}
