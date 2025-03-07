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

package org.springframework.ws.soap.server.endpoint.adapter.method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.AbstractMethodArgumentResolverTest;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
public class SoapMethodArgumentResolverTest extends AbstractMethodArgumentResolverTest {

	private SoapMethodArgumentResolver resolver;

	private MethodParameter soapMessageParameter;

	private MethodParameter soapEnvelopeParameter;

	private MethodParameter soapBodyParameter;

	private MethodParameter soapHeaderParameter;

	@BeforeEach
	public void setUp() throws Exception {

		this.resolver = new SoapMethodArgumentResolver();
		this.soapMessageParameter = new MethodParameter(getClass().getMethod("soapMessage", SoapMessage.class), 0);
		this.soapEnvelopeParameter = new MethodParameter(getClass().getMethod("soapEnvelope", SoapEnvelope.class), 0);
		this.soapBodyParameter = new MethodParameter(getClass().getMethod("soapBody", SoapBody.class), 0);
		this.soapHeaderParameter = new MethodParameter(getClass().getMethod("soapHeader", SoapHeader.class), 0);
	}

	@Test
	public void supportsParameter() {

		assertThat(this.resolver.supportsParameter(this.soapMessageParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapEnvelopeParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapBodyParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapHeaderParameter)).isTrue();
	}

	@Test
	public void resolveSoapMessageSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapMessageParameter);

		assertThat(result).isEqualTo(messageContext.getRequest());
	}

	@Test
	public void resolveSoapEnvelopeSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapEnvelopeParameter);

		assertThat(result).isEqualTo(((SoapMessage) messageContext.getRequest()).getEnvelope());
	}

	@Test
	public void resolveSoapBodySaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapBodyParameter);

		assertThat(result).isEqualTo(((SoapMessage) messageContext.getRequest()).getSoapBody());
	}

	@Test
	public void resolveSoapHeaderSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapHeaderParameter);

		assertThat(result).isEqualTo(((SoapMessage) messageContext.getRequest()).getSoapHeader());
	}

	public void soapMessage(SoapMessage soapMessage) {

	}

	public void soapEnvelope(SoapEnvelope soapEnvelope) {
	}

	public void soapBody(SoapBody soapBody) {
	}

	public void soapHeader(SoapHeader soapHeader) {
	}

}
