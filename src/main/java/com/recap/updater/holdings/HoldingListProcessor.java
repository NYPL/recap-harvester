package com.recap.updater.holdings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.recap.constants.Constants;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.Holding;
import com.recap.xml.models.Holdings;

public class HoldingListProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		BibRecord bibRecord = (BibRecord) exchangeContents.get(Constants.BIB_RECORD);
		List<Holding> listOfHolding = getHoldings(bibRecord);
		exchangeContents.put(Constants.LIST_HOLDING, listOfHolding);
		System.out.println(listOfHolding);
		exchange.getIn().setBody(exchangeContents);
	}
	
	public List<Holding> getHoldings(BibRecord bibRecord){
		List<Holding> listHolding = new ArrayList<>();
		for(Holdings holdings : bibRecord.getHoldings()){
			for(Holding holding : holdings.getHolding()){
				listHolding.add(holding);
			}
		}
		return listHolding;
	}

}
