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

package org.springframework.ws.soap.saaj;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapVersion;

/**
 * SAAJ-specific implementation of the {@code SoapEnvelope} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPEnvelope}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapEnvelope extends SaajSoapElement<SOAPEnvelope> implements SoapEnvelope {

	private SaajSoapBody body;

	private SaajSoapHeader header;

	private final boolean langAttributeOnSoap11FaultString;

	SaajSoapEnvelope(SOAPEnvelope element, boolean langAttributeOnSoap11FaultString) {
		super(element);
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
	}

	@Override
	public SoapBody getBody() {
		if (this.body == null) {
			try {
				SOAPBody saajBody = getSaajEnvelope().getBody();
				if (saajBody == null) {
					throw new SaajSoapBodyException("SAAJ SOAP message has no body");
				}
				if (saajBody.getElementQName()
					.getNamespaceURI()
					.equals(SoapVersion.SOAP_11.getEnvelopeNamespaceUri())) {
					this.body = new SaajSoap11Body(saajBody, this.langAttributeOnSoap11FaultString);
				}
				else {
					this.body = new SaajSoap12Body(saajBody);
				}
			}
			catch (SOAPException ex) {
				throw new SaajSoapBodyException(ex);
			}
		}
		return this.body;
	}

	@Override
	public SoapHeader getHeader() {
		if (this.header == null) {
			try {
				SOAPHeader saajHeader = getSaajEnvelope().getHeader();
				if (saajHeader != null) {
					if (saajHeader.getElementQName()
						.getNamespaceURI()
						.equals(SoapVersion.SOAP_11.getEnvelopeNamespaceUri())) {
						this.header = new SaajSoap11Header(saajHeader);
					}
					else {
						this.header = new SaajSoap12Header(saajHeader);
					}
				}
				else {
					this.header = null;
				}
			}
			catch (SOAPException ex) {
				throw new SaajSoapHeaderException(ex);
			}
		}
		return this.header;
	}

	protected SOAPEnvelope getSaajEnvelope() {
		return getSaajElement();
	}

}
