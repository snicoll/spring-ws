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

package org.springframework.ws.server.endpoint;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for AbstractEndpointExceptionResolver
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 */
public class EndpointExceptionResolverTest {

	private MethodEndpoint methodEndpoint;

	private AbstractEndpointExceptionResolver exceptionResolver;

	@BeforeEach
	public void setUp() throws Exception {

		this.exceptionResolver = new AbstractEndpointExceptionResolver() {

			@Override
			protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
				return true;
			}
		};

		this.exceptionResolver.setMappedEndpoints(Collections.singleton(this));
		this.methodEndpoint = new MethodEndpoint(this, getClass().getMethod("emptyMethod"));
	}

	@Test
	public void testMatchMethodEndpoint() {

		boolean matched = this.exceptionResolver.resolveException(null, this.methodEndpoint, null);

		assertThat(matched).isTrue();
	}

	public void emptyMethod() {
	}

}
