package com.recap.updater.holdings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.Holdings;

public class HoldingsProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeWithBibAndHoldings = (Map<String, Object>) exchange.getIn().getBody();
		Bib bib = (Bib) exchangeWithBibAndHoldings.get(Constants.BIB);
		BibRecord bibRecord = (BibRecord) exchangeWithBibAndHoldings.get(Constants.BIB_RECORD);
		List<Holdings> holdings = getHoldings(bibRecord);
		Map<String, Object> bibAndHoldings = new HashMap<>();
		bibAndHoldings.put(Constants.BIB, bib);
		bibAndHoldings.put(Constants.HOLDINGS, holdings);
		exchange.getIn().setBody(bibAndHoldings);
	}
	
	private List<Holdings> getHoldings(BibRecord bibRecord){
		List<Holdings> holdings = bibRecord.getHoldings();
		return holdings;
	}

}
