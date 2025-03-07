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

package org.springframework.ws.transport.mail;

import java.net.URI;
import java.util.Collections;

import javax.xml.namespace.QName;

import com.icegreen.greenmail.spring.GreenMailBean;
import jakarta.mail.Address;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;

import static org.assertj.core.api.Assertions.assertThat;

public class MailMessageSenderIntegrationTest {

	private MailMessageSender messageSender;

	private MessageFactory messageFactory;

	private static final String SOAP_ACTION = "http://springframework.org/DoIt";

	private GreenMailBean greenMailBean;

	@BeforeEach
	public void setUp() throws Exception {

		this.greenMailBean = new GreenMailBean();
		this.greenMailBean.setAutostart(true);
		this.greenMailBean.setSmtpProtocol(true);
		this.greenMailBean.setImapProtocol(true);
		this.greenMailBean.setUsers(Collections.singletonList("system:password@localhost"));
		this.greenMailBean.afterPropertiesSet();

		this.messageSender = new MailMessageSender();
		this.messageSender.setFrom("Spring-WS SOAP Client <client@localhost>");
		this.messageSender.setTransportUri("smtp://localhost:" + this.greenMailBean.getGreenMail().getSmtp().getPort());
		this.messageSender
			.setStoreUri("imap://localhost:" + this.greenMailBean.getGreenMail().getImap().getPort() + "/INBOX");
		this.messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		this.messageSender.afterPropertiesSet();
	}

	@AfterEach
	void tearDown() throws Exception {
		this.greenMailBean.destroy();
	}

	@Disabled
	@Test
	public void testSendAndReceiveQueueNoResponse() throws Exception {

		URI mailTo = new URI("mailto:server@localhost?subject=SOAP%20Test");

		try (WebServiceConnection connection = this.messageSender.createConnection(mailTo)) {

			SOAPMessage saajMessage = this.messageFactory.createMessage();
			saajMessage.getSOAPBody().addBodyElement(new QName("http://springframework.org", "test"));
			SoapMessage soapRequest = new SaajSoapMessage(saajMessage);
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			MimeMessage[] receivedMessages = this.greenMailBean.getGreenMail().getReceivedMessages();
			assertThat(receivedMessages).hasSize(1);
			assertThat(receivedMessages[0].getAllRecipients()).extracting(Address::toString)
				.contains("server@localhost");
		}
	}

}
