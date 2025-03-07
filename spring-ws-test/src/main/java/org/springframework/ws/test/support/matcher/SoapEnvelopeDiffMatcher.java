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

package org.springframework.ws.test.support.matcher;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.TransformerHelper;

import static org.springframework.ws.test.support.AssertionErrors.assertTrue;
import static org.springframework.ws.test.support.AssertionErrors.fail;

/**
 * Matches {@link Source} SOAP envelopes.
 *
 * @author Alexander Shutyaev
 * @since 2.1.1
 * @deprecated Migrate to
 * {@link org.springframework.ws.test.support.matcher.xmlunit2.SoapEnvelopeDiffMatcher}.
 */
@Deprecated
public class SoapEnvelopeDiffMatcher extends AbstractSoapMessageMatcher {

	private final Source expected;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	static {
		XMLUnit.setIgnoreWhitespace(true);
	}

	public SoapEnvelopeDiffMatcher(Source expected) {

		Assert.notNull(expected, "'expected' must not be null");
		this.expected = expected;
	}

	@Override
	protected void match(SoapMessage soapMessage) throws IOException, AssertionError {

		Document actualDocument = soapMessage.getDocument();
		Document expectedDocument = createDocumentFromSource(this.expected);
		Diff diff = new Diff(expectedDocument, actualDocument);
		assertTrue("Envelopes are different, " + diff, diff.similar());
	}

	private Document createDocumentFromSource(Source source) {

		try {
			DOMResult result = new DOMResult();
			this.transformerHelper.transform(source, result);
			return (Document) result.getNode();
		}
		catch (TransformerException ex) {
			fail("Could not transform source to DOMResult" + ex.getMessage());
			return null;
		}
	}

}
