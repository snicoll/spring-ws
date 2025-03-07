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

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class Wss4jMessageInterceptorSignTest extends Wss4jTest {

	protected Wss4jSecurityInterceptor interceptor;

	@Override
	protected void onSetup() throws Exception {

		this.interceptor = new Wss4jSecurityInterceptor();
		this.interceptor.setValidationActions("Signature");

		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		Properties cryptoFactoryBeanConfig = new Properties();
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.provider",
				"org.apache.ws.security.components.crypto.Merlin");
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jceks");
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "123456");

		// from the class path
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.file", "private.jks");
		cryptoFactoryBean.setConfiguration(cryptoFactoryBeanConfig);
		cryptoFactoryBean.afterPropertiesSet();
		this.interceptor.setValidationSignatureCrypto(cryptoFactoryBean.getObject());
		this.interceptor.setSecurementSignatureCrypto(cryptoFactoryBean.getObject());
		this.interceptor.afterPropertiesSet();
	}

	@Test
	public void testValidateCertificate() throws Exception {

		SoapMessage message = loadSoap11Message("signed-soap.xml");

		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		this.interceptor.validateMessage(message, messageContext);
		Object result = getMessage(message);

		assertThat(result).isNotNull();

		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
	}

	@Test
	public void testValidateCertificateWithSignatureConfirmation() throws Exception {

		SoapMessage message = loadSoap11Message("signed-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);
		this.interceptor.setEnableSignatureConfirmation(true);
		this.interceptor.validateMessage(message, messageContext);
		WebServiceMessage response = messageContext.getResponse();
		this.interceptor.secureMessage(message, messageContext);

		assertThat(response).isNotNull();

		Document document = getDocument((SoapMessage) response);
		assertXpathExists("Absent SignatureConfirmation element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse11:SignatureConfirmation", document);
	}

	@Test
	public void testSignResponse() throws Exception {

		this.interceptor.setSecurementActions("Signature");
		this.interceptor.setEnableSignatureConfirmation(false);
		this.interceptor.setSecurementPassword("123456");
		this.interceptor.setSecurementUsername("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		// interceptor.setSecurementSignatureKeyIdentifier("IssuerSerial");

		this.interceptor.secureMessage(message, messageContext);

		Document document = getDocument(message);
		assertXpathExists("Absent SignatureConfirmation element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature", document);
	}

	@Test
	public void testSignResponseWithSignatureUser() throws Exception {

		this.interceptor.setSecurementActions("Signature");
		this.interceptor.setEnableSignatureConfirmation(false);
		this.interceptor.setSecurementPassword("123456");
		this.interceptor.setSecurementSignatureUser("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		this.interceptor.secureMessage(message, messageContext);

		Document document = getDocument(message);
		assertXpathExists("Absent SignatureConfirmation element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature", document);
	}

}
