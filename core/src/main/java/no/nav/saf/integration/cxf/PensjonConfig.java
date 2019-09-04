package no.nav.saf.integration.cxf;

import no.nav.saf.integration.sts.STSConfig;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.xml.namespace.QName;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class PensjonConfig extends AbstractCxfEndpointConfig {

	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/pensjonSak/v1/Binding";
	private static final QName SERVICE_QNAME = new QName(NAMESPACE, "PensjonSak_v1");
	private static final QName PORT_QNAME = new QName(NAMESPACE, "PensjonSak_v1Port");
	private static final String WSDL_URL = "no/nav/tjeneste/virksomhet/pensjonSak/v1/Binding.wsdl";

	@Inject
	public PensjonConfig(Bus bus, STSConfig stsConfig) {
		super(bus, stsConfig);
	}

	@Bean
	public PensjonSakV1 pensjonSakV1(@Value("${pensjon.v1.endpointurl}") String endpointurl,
									 @Value("${pensjon.v1.readtimeoutms}") int readtimeoutms,
									 @Value("${pensjon.v1.connectiontimeoutms}") int connectiontimeoutms) {
		setWsdlUrl(WSDL_URL);
		setServiceName(SERVICE_QNAME);
		setEndpointName(PORT_QNAME);
		setAdress(endpointurl);
		setReceiveTimeout(readtimeoutms);
		setConnectTimeout(connectiontimeoutms);
		addFeature(new WSAddressingFeature());
		addOutInterceptor(new LoggingOutInterceptor());
		PensjonSakV1 pensjonSakV1 = createPort(PensjonSakV1.class);
		configureSTSSamlToken(pensjonSakV1);
		return pensjonSakV1;
	}

}


