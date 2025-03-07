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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.test.support.AssertionErrors;

/**
 * Implementation of {@link ResponseCreator} that responds with a SOAP fault.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class SoapFaultResponseCreator extends AbstractResponseCreator {

	@Override
	protected void doWithResponse(URI uri, WebServiceMessage request, WebServiceMessage response) throws IOException {
		if (!(response instanceof SoapMessage soapResponse)) {
			AssertionErrors.fail("Response is not a SOAP message");
			return;
		}
		SoapBody responseBody = soapResponse.getSoapBody();
		if (responseBody == null) {
			AssertionErrors.fail("SOAP message [" + response + "] does not contain SOAP body");
		}
		addSoapFault(responseBody);
	}

	/**
	 * Abstract template method that allows subclasses to add a SOAP Fault to the given
	 * Body.
	 * @param soapBody the body to attach a fault to
	 */
	protected abstract void addSoapFault(SoapBody soapBody);

}
