package no.nav.saf.integration.cxf;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class AktoerV2Config extends AbstractCxfEndpointConfig {

	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/aktoer/v2";
	private static final QName SERVICE_QNAME = new QName(NAMESPACE, "Aktoer_v2");
	private static final QName PORT_QNAME = new QName(NAMESPACE, "Aktoer_v2Port");
	private static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/aktoer/v2/v2.wsdl";

	@Bean
	public AktoerV2 aktoerV2(@Value("${aktoer.v2.endpointurl}") String endpointurl,
							 @Value("${aktoer.v2.readtimeoutms}") int readtimeoutms,
							 @Value("${aktoer.v2.connectiontimeoutms}") int connectiontimeoutms) {
		setWsdlUrl(WSDL_URL);
		setServiceName(SERVICE_QNAME);
		setEndpointName(PORT_QNAME);
		setAdress(endpointurl);
		setReceiveTimeout(readtimeoutms);
		setConnectTimeout(connectiontimeoutms);
		addFeature(new WSAddressingFeature());
		addLoggingInInterceptor();
		addLoggingOutInterceptor();
//		addHandler(new MDCUsernameTokenOutHandler());  TODO Add CallId and AppId to SOAP header?
		AktoerV2 aktoerV2 = createPort(AktoerV2.class);
		configureSTSSamlToken(aktoerV2);
		return aktoerV2;
	}

}


