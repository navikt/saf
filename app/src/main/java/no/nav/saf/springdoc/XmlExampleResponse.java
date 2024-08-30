package no.nav.saf.springdoc;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Value;

@Value
@XmlRootElement(name = "skanningmetadata")
public class XmlExampleResponse {
	String referansenummer;
}
