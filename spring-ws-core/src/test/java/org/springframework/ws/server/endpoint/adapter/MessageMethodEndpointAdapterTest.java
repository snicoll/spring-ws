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

package org.springframework.ws.server.endpoint.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

@Deprecated
public class MessageMethodEndpointAdapterTest {

	private MessageMethodEndpointAdapter adapter;

	private boolean supportedInvoked;

	private MessageContext messageContext;

	@BeforeEach
	public void setUp() {
		this.adapter = new MessageMethodEndpointAdapter();
		this.messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@Test
	public void testSupported() throws NoSuchMethodException {

		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "supported", MessageContext.class);
		assertThat(this.adapter.supportsInternal(methodEndpoint)).isTrue();
	}

	@Test
	public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {

		assertThat(this.adapter.supportsInternal(
				new MethodEndpoint(this, "unsupportedMultipleParams", MessageContext.class, MessageContext.class)))
			.isFalse();
	}

	@Test
	public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {

		assertThat(this.adapter.supportsInternal(new MethodEndpoint(this, "unsupportedWrongParam", String.class)))
			.isFalse();
	}

	@Test
	public void testInvokeSupported() throws Exception {

		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "supported", MessageContext.class);

		assertThat(this.supportedInvoked).isFalse();

		this.adapter.invoke(this.messageContext, methodEndpoint);

		assertThat(this.supportedInvoked).isTrue();
	}

	public void supported(MessageContext context) {
		this.supportedInvoked = true;
	}

	public void unsupportedMultipleParams(MessageContext s1, MessageContext s2) {
	}

	public void unsupportedWrongParam(String request) {
	}

}
