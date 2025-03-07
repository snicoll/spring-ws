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

package org.springframework.ws.soap.addressing.client;

import java.net.URI;

import javax.xml.namespace.QName;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.addressing.AbstractWsAddressingTest;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public abstract class AbstractActionCallbackTest extends AbstractWsAddressingTest {

	private ActionCallback callback;

	private MessageIdStrategy strategyMock;

	private WebServiceConnection connectionMock;

	@BeforeEach
	public void createMocks() {

		this.strategyMock = createMock(MessageIdStrategy.class);

		this.connectionMock = createMock(WebServiceConnection.class);

		TransportContext transportContext = new DefaultTransportContext(this.connectionMock);
		TransportContextHolder.setTransportContext(transportContext);
	}

	@AfterEach
	public void clearContext() {
		TransportContextHolder.setTransportContext(null);
	}

	@Test
	public void testValid() throws Exception {

		URI action = new URI("http://example.com/fabrikam/mail/Delete");
		URI to = new URI("mailto:fabrikam@example.com");
		this.callback = new ActionCallback(action, getVersion(), to);
		this.callback.setMessageIdStrategy(this.strategyMock);
		SaajSoapMessage message = createDeleteMessage();
		expect(this.strategyMock.newMessageId(message)).andReturn(new URI("http://example.com/someuniquestring"));
		this.callback.setReplyTo(new EndpointReference(new URI("http://example.com/business/client1")));

		replay(this.strategyMock, this.connectionMock);

		this.callback.doWithMessage(message);

		SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/valid.xml");

		assertXMLNotSimilar(expected, message);

		verify(this.strategyMock, this.connectionMock);
	}

	@Test
	public void testDefaults() throws Exception {

		URI action = new URI("http://example.com/fabrikam/mail/Delete");
		URI connectionUri = new URI("mailto:fabrikam@example.com");
		this.callback = new ActionCallback(action, getVersion());
		this.callback.setMessageIdStrategy(this.strategyMock);
		this.callback.setShouldInitializeTo(true);
		expect(this.connectionMock.getUri()).andReturn(connectionUri);

		SaajSoapMessage message = createDeleteMessage();
		expect(this.strategyMock.newMessageId(message)).andReturn(new URI("http://example.com/someuniquestring"));
		this.callback.setReplyTo(new EndpointReference(new URI("http://example.com/business/client1")));

		replay(this.strategyMock, this.connectionMock);

		this.callback.doWithMessage(message);

		SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/valid.xml");

		assertXMLNotSimilar(expected, message);

		verify(this.strategyMock, this.connectionMock);
	}

	@Test
	public void testNotInitializeTo() throws Exception {

		URI action = new URI("http://example.com/fabrikam/mail/Delete");
		URI connectionUri = new URI("mailto:fabrikam@example.com");
		this.callback = new ActionCallback(action, getVersion());
		this.callback.setMessageIdStrategy(this.strategyMock);
		this.callback.setShouldInitializeTo(false);
		expect(this.connectionMock.getUri()).andReturn(connectionUri).times(0, 1);

		SaajSoapMessage message = createDeleteMessage();
		expect(this.strategyMock.newMessageId(message)).andReturn(new URI("http://example.com/someuniquestring"));
		this.callback.setReplyTo(new EndpointReference(new URI("http://example.com/business/client1")));

		replay(this.strategyMock, this.connectionMock);

		this.callback.doWithMessage(message);

		SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/request-without-shouldInitializeTo.xml");

		assertXMLSimilar(expected, message);

		verify(this.strategyMock, this.connectionMock);
	}

	private SaajSoapMessage createDeleteMessage() throws SOAPException {

		SOAPMessage saajMessage = this.messageFactory.createMessage();
		SOAPBody saajBody = saajMessage.getSOAPBody();
		SOAPBodyElement delete = saajBody.addBodyElement(new QName("http://example.com/fabrikam", "Delete"));
		SOAPElement maxCount = delete.addChildElement(new QName("http://example.com/fabrikam", "maxCount"));
		maxCount.setTextContent("42");
		return new SaajSoapMessage(saajMessage);
	}

	protected abstract AddressingVersion getVersion();

	protected abstract String getTestPath();

}
