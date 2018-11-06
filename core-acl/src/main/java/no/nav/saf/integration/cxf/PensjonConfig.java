package no.nav.saf.integration.cxf;

import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class PensjonConfig extends AbstractCxfEndpointConfig {

	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/pensjonsak/v1";
	private static final QName SERVICE_QNAME = new QName(NAMESPACE, "PensjonSak_v1");
	private static final QName PORT_QNAME = new QName(NAMESPACE, "PensjonSak_v1Port");
	private static final String WSDL_URL = "no/nav/tjeneste/virksomhet/pensjonSak/v1/PensjonSak.wsdl";


	@Bean
	public PensjonSakV1 PensjonSakV1(@Value("${pensjonsak.v1.endpointurl}") String endpointurl,
									 @Value("${pensjonsak.v1.readtimeoutms}") int readtimeoutms,
									 @Value("${pensjonsak.v1.connectiontimeoutms}") int connectiontimeoutms) {
		setWsdlUrl(WSDL_URL);
		setServiceName(SERVICE_QNAME);
		setEndpointName(PORT_QNAME);
		setAdress(endpointurl);
		setReceiveTimeout(readtimeoutms);
		setConnectTimeout(connectiontimeoutms);
		addFeature(new WSAddressingFeature());

		PensjonSakV1 pensjonSakV1 = createPort(PensjonSakV1.class);
		configureSTSSamlToken(pensjonSakV1);
		return pensjonSakV1;
	}

}


