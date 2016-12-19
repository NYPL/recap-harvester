package com.recap.updater.holdings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.Holding;
import com.recap.xml.models.Holdings;

public class HoldingListProcessor implements Processor{
	
	private static Logger logger = LoggerFactory.getLogger(HoldingListProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		BibRecord bibRecord = (BibRecord) exchangeContents.get(Constants.BIB_RECORD);
		List<Holding> holdings = getHoldings(bibRecord);
		exchangeContents.put(Constants.LIST_HOLDING, holdings);
		exchange.getIn().setBody(exchangeContents);
	}
	
	public List<Holding> getHoldings(BibRecord bibRecord){
		List<Holding> listHolding = new ArrayList<>();
		if(bibRecord.getHoldings() == null){
			System.out.println(bibRecord.getBib().getOwningInstitutionBibId());
			logger.error("Unable to get holdings for bib - " + 
			bibRecord.getBib().getOwningInstitutionBibId());
		}
		for(Holdings holdings : bibRecord.getHoldings()){
			for(Holding holding : holdings.getHolding()){
				listHolding.add(holding);
			}
		}
		return listHolding;
	}

}
