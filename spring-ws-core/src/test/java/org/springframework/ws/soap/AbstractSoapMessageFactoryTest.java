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

package org.springframework.ws.soap;

import org.junit.jupiter.api.Test;

import org.springframework.ws.AbstractWebServiceMessageFactoryTest;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AbstractSoapMessageFactoryTest extends AbstractWebServiceMessageFactoryTest {

	@Test
	public void testCreateEmptySoapMessage() {

		WebServiceMessage message = this.messageFactory.createWebServiceMessage();

		assertThat(message).isInstanceOf(SoapMessage.class);
	}

	@Test
	public void testCreateSoapMessageIllFormedXml() {
		assertThatExceptionOfType(InvalidXmlException.class).isThrownBy(this::doTestCreateSoapMessageIllFormedXml);
	}

	public abstract void doTestCreateSoapMessageIllFormedXml() throws Exception;

	@Test
	public abstract void testCreateSoapMessageNoAttachment() throws Exception;

	@Test
	public abstract void testCreateSoapMessageSwA() throws Exception;

	@Test
	public abstract void testCreateSoapMessageMtom() throws Exception;

	@Test
	public abstract void testCreateSoapMessageMissingContentType() throws Exception;

}
