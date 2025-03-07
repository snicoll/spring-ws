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

package org.springframework.ws.soap.addressing;

import java.io.IOException;
import java.io.InputStream;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.saaj.SaajSoapMessage;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractWsAddressingTest {

	protected MessageFactory messageFactory;

	@BeforeEach
	public void createMessageFactory() throws Exception {
		this.messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
	}

	protected SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", " application/soap+xml");
		InputStream is = AbstractWsAddressingTest.class.getResourceAsStream(fileName);

		try (is) {
			assertThat(is).isNotNull();
			return new SaajSoapMessage(this.messageFactory.createMessage(mimeHeaders, is));
		}
	}

	protected void assertXMLSimilar(SaajSoapMessage expected, SaajSoapMessage result) {

		Document expectedDocument = expected.getSaajMessage().getSOAPPart();
		Document resultDocument = result.getSaajMessage().getSOAPPart();

		XmlAssert.assertThat(resultDocument)
			.and(expectedDocument) //
			.ignoreWhitespace() //
			.ignoreChildNodesOrder() //
			.areSimilar();
	}

	protected void assertXMLNotSimilar(SaajSoapMessage expected, SaajSoapMessage result) {

		Document expectedDocument = expected.getSaajMessage().getSOAPPart();
		Document resultDocument = result.getSaajMessage().getSOAPPart();

		XmlAssert.assertThat(resultDocument)
			.and(expectedDocument) //
			.ignoreWhitespace() //
			.areNotSimilar();
	}

}
