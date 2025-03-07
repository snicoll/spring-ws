/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;

import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SaajWss4jMessageInterceptorSignTest extends Wss4jMessageInterceptorSignTest {

	private static final String PAYLOAD = "<tru:StockSymbol xmlns:tru=\"http://fabrikam123.com/payloads\">QQQ</tru:StockSymbol>";

	@Test
	public void testSignAndValidate() throws Exception {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		this.interceptor.setSecurementActions("Signature");
		this.interceptor.setEnableSignatureConfirmation(false);
		this.interceptor.setSecurementPassword("123456");
		this.interceptor.setSecurementUsername("rsaKey");
		SOAPMessage saajMessage = this.saajSoap11MessageFactory.createMessage();
		transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
		SaajSoapMessage message = new SaajSoapMessage(saajMessage, this.saajSoap11MessageFactory);
		MessageContext messageContext = new DefaultMessageContext(message,
				new SaajSoapMessageFactory(this.saajSoap11MessageFactory));

		this.interceptor.secureMessage(message, messageContext);

		SOAPHeader header = message.getSaajMessage().getSOAPHeader();
		Iterator<?> iterator = header.getChildElements(new QName(
				"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security"));

		assertThat(iterator.hasNext()).isTrue();

		SOAPHeaderElement securityHeader = (SOAPHeaderElement) iterator.next();
		iterator = securityHeader.getChildElements(new QName("http://www.w3.org/2000/09/xmldsig#", "Signature"));

		assertThat(iterator.hasNext()).isTrue();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		message.writeTo(bos);

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		SOAPMessage signed = this.saajSoap11MessageFactory.createMessage(mimeHeaders, bis);
		message = new SaajSoapMessage(signed, this.saajSoap11MessageFactory);
		messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(this.saajSoap11MessageFactory));

		this.interceptor.validateMessage(message, messageContext);
	}

	@Test
	public void testSignWithoutInclusivePrefixesAndValidate() throws Exception {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		this.interceptor.setSecurementActions("Signature");
		this.interceptor.setEnableSignatureConfirmation(false);
		this.interceptor.setSecurementPassword("123456");
		this.interceptor.setSecurementUsername("rsaKey");
		this.interceptor.setAddInclusivePrefixes(false);
		SOAPMessage saajMessage = this.saajSoap11MessageFactory.createMessage();
		transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
		SaajSoapMessage message = new SaajSoapMessage(saajMessage, this.saajSoap11MessageFactory);
		MessageContext messageContext = new DefaultMessageContext(message,
				new SaajSoapMessageFactory(this.saajSoap11MessageFactory));

		this.interceptor.secureMessage(message, messageContext);

		SOAPHeader header = message.getSaajMessage().getSOAPHeader();
		Iterator<?> iterator = header.getChildElements(new QName(
				"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security"));

		assertThat(iterator.hasNext()).isTrue();

		SOAPHeaderElement securityHeader = (SOAPHeaderElement) iterator.next();
		iterator = securityHeader.getChildElements(new QName("http://www.w3.org/2000/09/xmldsig#", "Signature"));

		assertThat(iterator.hasNext()).isTrue();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		message.writeTo(bos);

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		SOAPMessage signed = this.saajSoap11MessageFactory.createMessage(mimeHeaders, bis);
		message = new SaajSoapMessage(signed, this.saajSoap11MessageFactory);
		messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(this.saajSoap11MessageFactory));

		this.interceptor.validateMessage(message, messageContext);
	}

}
