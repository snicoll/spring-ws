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

package org.springframework.ws.server.endpoint.adapter.method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageContextMethodArgumentResolverTest {

	private MessageContextMethodArgumentResolver resolver;

	private MethodParameter supported;

	@BeforeEach
	public void setUp() throws NoSuchMethodException {

		this.resolver = new MessageContextMethodArgumentResolver();
		this.supported = new MethodParameter(getClass().getMethod("supported", MessageContext.class), 0);
	}

	@Test
	public void supportsParameter() {
		assertThat(this.resolver.supportsParameter(this.supported)).isTrue();
	}

	@Test
	public void resolveArgument() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MessageContext result = this.resolver.resolveArgument(messageContext, this.supported);

		assertThat(result).isSameAs(messageContext);
	}

	public void supported(MessageContext messageContext) {
	}

}
