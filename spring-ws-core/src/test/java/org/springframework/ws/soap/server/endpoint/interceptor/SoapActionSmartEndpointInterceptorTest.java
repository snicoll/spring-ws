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

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SoapActionSmartEndpointInterceptorTest {

	private EndpointInterceptor delegate;

	private String soapAction;

	private MessageContext messageContext;

	@BeforeEach
	public void setUp() {

		this.delegate = new EndpointInterceptorAdapter();

		this.soapAction = "http://springframework.org/spring-ws";

		SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
		messageFactory.afterPropertiesSet();
		SaajSoapMessage request = messageFactory.createWebServiceMessage();
		request.setSoapAction(this.soapAction);
		this.messageContext = new DefaultMessageContext(request, messageFactory);
	}

	@Test
	public void neitherNamespaceNorLocalPart() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new SoapActionSmartEndpointInterceptor(this.delegate, null));
	}

	@Test
	public void shouldInterceptMatch() {

		SoapActionSmartEndpointInterceptor interceptor = new SoapActionSmartEndpointInterceptor(this.delegate,
				this.soapAction);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isTrue();
	}

	@Test
	public void shouldInterceptNonMatch() {

		SoapActionSmartEndpointInterceptor interceptor = new SoapActionSmartEndpointInterceptor(this.delegate,
				"http://springframework.org/other");

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isFalse();
	}

}
