package com.recap.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Bib;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsJsonProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.xml.models.BibRecord;

@Component
public class ReCapXmlRouteBuilderPublisher extends RouteBuilder{
	
	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;
	
	@Autowired
	private BaseConfig baseConfig;
	
	private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilderPublisher.class);

	@Override
	public void configure() throws Exception {
		onException(RecapHarvesterException.class)
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, 
						Throwable.class);
				logger.error("RECAPHARVESTER ERROR HANDLED - ", caught);
			}
		})
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setHeader("rabbitmq.ROUTING_KEY", "failure");
			}
		})
		.to("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
		System.getenv("rabbitmq_port") + "/recap_xchange?"
				+ "connectionFactory=#rabbitConnectionFactory&"
				+ "queue=recap_failures&"
				+ "autoDelete=false&"
				+ "routingKey=failure&"
				+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
				+ "automaticRecoveryEnabled=true")
		.handled(true);
		
		onException(Exception.class)
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, 
						Throwable.class);
				logger.error("APP FATAL UNEXPECTED ERROR - ", caught);
			}
		})
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setHeader("rabbitmq.ROUTING_KEY", "failure");
			}
		})
		.to("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
				System.getenv("rabbitmq_port") + "/recap_xchange?"
						+ "connectionFactory=#rabbitConnectionFactory&"
						+ "queue=recap_failures&"
						+ "autoDelete=false&"
						+ "routingKey=failure&"
						+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
						+ "automaticRecoveryEnabled=true")
		.handled(true);
		
		from("file:" + scsbexportstaging + "?fileName=testrecord.xml&"
				+ "maxMessagesPerPoll=1&noop=true")
		.split(body().tokenizeXML("bibRecord", ""))
		.streaming()
		.unmarshal("getBibRecordJaxbDataFormat")
		.multicast()
		.to("direct:bib", "direct:item");
		
		from("direct:bib")
		.process(new BibProcessor(baseConfig))
		.process(new BibJsonProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setHeader("rabbitmq.ROUTING_KEY", "recap_bibs");
			}
		})
		.to("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
				System.getenv("rabbitmq_port") + "/recap_xchange?"
				+ "connectionFactory=#rabbitConnectionFactory&"
				+ "queue=recap_bibs&"
				+ "autoDelete=false&"
				+ "routingKey=recap_bibs&"
				+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
				+ "automaticRecoveryEnabled=true");
		
		from("direct:item")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				Map<String, Object> exchangeContents = new HashMap<>();
				BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
				exchangeContents.put(Constants.BIB_RECORD, bibRecord);
				exchange.getIn().setBody(exchangeContents);
			}
		})
		.process(new HoldingListProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				Map<String, Object> exchangeContents = (Map<String, Object>)
						exchange.getIn().getBody();
				Bib bib = new BibProcessor(baseConfig).getBibFromBibRecord(
						(BibRecord) exchangeContents.get(Constants.BIB_RECORD));
				exchangeContents.put(Constants.BIB, bib);
				exchange.getIn().setBody(exchangeContents);
			}
		})
		.process(new ItemsProcessor())
		.process(new ItemsJsonProcessor())
		.split(body())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				exchange.getIn().setHeader("rabbitmq.ROUTING_KEY", "recap_items");
			}
		})
		.to("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
				System.getenv("rabbitmq_port") + "/recap_xchange?"
				+ "connectionFactory=#rabbitConnectionFactory&"
				+ "queue=recap_items&"
				+ "autoDelete=false&"
				+ "routingKey=recap_items&"
				+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
				+ "automaticRecoveryEnabled=true");
	}

}
