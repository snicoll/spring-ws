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

import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.saml.SAMLCallback;
import org.apache.wss4j.common.saml.bean.KeyInfoBean;
import org.apache.wss4j.common.saml.bean.SubjectBean;
import org.apache.wss4j.common.saml.bean.Version;
import org.apache.wss4j.common.saml.builder.SAML2Constants;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

public abstract class Wss4jMessageInterceptorSamlTest extends Wss4jTest {

	protected Wss4jSecurityInterceptor interceptor;

	@Override
	protected void onSetup() throws Exception {

		interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("SAMLTokenSigned");
		interceptor.setValidationActions("SAMLTokenSigned Signature");
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setCryptoProvider(Merlin.class);
		cryptoFactoryBean.setKeyStoreType("jceks");
		cryptoFactoryBean.setKeyStorePassword("123456");
		cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));
		cryptoFactoryBean.afterPropertiesSet();
		Crypto crypto = cryptoFactoryBean.getObject();

		CryptoType type = new CryptoType(CryptoType.TYPE.ALIAS);
		type.setAlias("rsaKey");
		X509Certificate userCertificate = crypto.getX509Certificates(type)[0];

		interceptor.setSecurementSignatureCrypto(crypto);
		interceptor.setValidationSignatureCrypto(crypto);
		interceptor.setSecurementSamlCallbackHandler(getSamlCalbackHandler(crypto, userCertificate));
		interceptor.afterPropertiesSet();
	}

	@Test
	public void testAddSAML() throws Exception {

		interceptor.setSecurementPassword("123456");
		interceptor.setSecurementUsername("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		interceptor.secureMessage(message, messageContext);
		Document document = getDocument(message);

		assertXpathExists("Absent SAML Assertion element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/saml:Assertion", document);

		// lets verify the signature that we've just generated
		interceptor.validateMessage(message, messageContext);
	}

	protected CallbackHandler getSamlCalbackHandler(Crypto crypto, X509Certificate userCert) {
		return new SamlCallbackHandler(crypto, userCert);
	}

	private static class SamlCallbackHandler implements CallbackHandler {

		private Crypto crypto;

		private X509Certificate userCertificate;

		public SamlCallbackHandler(Crypto crypto, X509Certificate userCertificate) {

			this.crypto = crypto;
			this.userCertificate = userCertificate;
		}

		@Override
		public void handle(Callback[] callbacks) {

			for (Callback value : callbacks) {
				if (value instanceof SAMLCallback callback) {

					callback.setSamlVersion(Version.SAML_20);
					callback.setIssuerCrypto(crypto);
					callback.setIssuerKeyName("rsaKey");
					callback.setIssuerKeyPassword("123456");
					callback.setIssuer("test-issuer");
					SubjectBean subject = new SubjectBean("test-subject", "", SAML2Constants.CONF_BEARER);
					KeyInfoBean keyInfo = new KeyInfoBean();
					keyInfo.setCertificate(userCertificate);
					subject.setKeyInfo(keyInfo);
					callback.setSubject(subject);
					callback.setSignAssertion(true);
				}
			}
		}

	}

}
