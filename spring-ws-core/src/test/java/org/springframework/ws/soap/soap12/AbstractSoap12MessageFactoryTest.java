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

package org.springframework.ws.soap.soap12;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.AbstractSoapMessageFactoryTest;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoap12MessageFactoryTest extends AbstractSoapMessageFactoryTest {

	@Override
	public void testCreateEmptyMessage() {

		WebServiceMessage message = this.messageFactory.createWebServiceMessage();

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_12);
	}

	@Override
	public void testCreateSoapMessageNoAttachment() throws Exception {

		InputStream is = AbstractSoap12MessageFactoryTest.class.getResourceAsStream("soap12.xml");
		Map<String, String> headers = new HashMap<>();
		String soapAction = "\"http://springframework.org/spring-ws/Action\"";
		headers.put(TransportConstants.HEADER_CONTENT_TYPE, "application/soap+xml; action=" + soapAction);
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = this.messageFactory.createWebServiceMessage(tis);
		assertThat(message).isInstanceOf(SoapMessage.class);
		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_12);
		assertThat(soapMessage.getSoapAction()).isEqualTo(soapAction);
		assertThat(soapMessage.isXopPackage()).isFalse();
	}

	@Override
	public void doTestCreateSoapMessageIllFormedXml() throws Exception {

		InputStream is = AbstractSoap12MessageFactoryTest.class.getResourceAsStream("soap12-ill-formed.xml");
		Map<String, String> headers = new HashMap<>();
		headers.put(TransportConstants.HEADER_CONTENT_TYPE, "application/soap+xml");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		this.messageFactory.createWebServiceMessage(tis);
	}

	@Override
	public void testCreateSoapMessageSwA() throws Exception {

		InputStream is = AbstractSoap12MessageFactoryTest.class.getResourceAsStream("soap12-attachment.bin");
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "multipart/related;" + "type=\"application/soap+xml\";"
				+ "boundary=\"----=_Part_0_11416420.1149699787554\"");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = this.messageFactory.createWebServiceMessage(tis);

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_12);
		assertThat(soapMessage.isXopPackage()).isFalse();

		Attachment attachment = soapMessage.getAttachment("interface21");

		assertThat(attachment).isNotNull();
	}

	@Override
	public void testCreateSoapMessageMtom() throws Exception {

		InputStream is = AbstractSoap12MessageFactoryTest.class.getResourceAsStream("soap12-mtom.bin");
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type",
				"multipart/related;" + "start-info=\"application/soap+xml\";" + "type=\"application/xop+xml\";"
						+ "start=\"<0.urn:uuid:40864869929B855F971176851454456@apache.org>\";"
						+ "boundary=\"MIMEBoundaryurn_uuid_40864869929B855F971176851454455\"");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = this.messageFactory.createWebServiceMessage(tis);
		assertThat(message).isInstanceOf(SoapMessage.class);
		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_12);
		assertThat(soapMessage.isXopPackage()).isTrue();

		Iterator<Attachment> iter = soapMessage.getAttachments();

		assertThat(iter.hasNext()).isTrue();

		Attachment attachment = soapMessage.getAttachment("<1.urn:uuid:40864869929B855F971176851454452@apache.org>");

		assertThat(attachment).isNotNull();
	}

	@Override
	public void testCreateSoapMessageMissingContentType() throws Exception {

		InputStream is = AbstractSoap12MessageFactoryTest.class.getResourceAsStream("soap12.xml");
		TransportInputStream tis = new MockTransportInputStream(is, Collections.emptyMap());

		WebServiceMessage message = this.messageFactory.createWebServiceMessage(tis);
		assertThat(message).isInstanceOf(SoapMessage.class);
		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_12);
	}

}
