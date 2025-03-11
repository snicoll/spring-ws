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

package org.springframework.ws.support;

import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlunit.builder.Input;

import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationHelperTest {

	private ObservationHelper helper;

	@BeforeEach
	void setUp() {
		this.helper = new ObservationHelper();
	}

	@Test
	void getRootElementStreamSource() {

		StringSource source = new StringSource("<root xmlns='http://springframework.org/spring-ws'><child/></root>");

		QName name = this.helper.getRootElement(source);
		assertThat(name.getLocalPart()).isEqualTo("root");
		assertThat(name.getNamespaceURI()).isEqualTo("http://springframework.org/spring-ws");
	}

	@Test
	void getRootElementDomSource() {

		DOMElement payloadElement = new DOMElement(
				new org.dom4j.QName("root", new Namespace(null, "http://springframework.org/spring-ws")));
		payloadElement.addElement("child");

		QName name = this.helper.getRootElement(Input.from(payloadElement).build());
		assertThat(name.getLocalPart()).isEqualTo("root");
		assertThat(name.getNamespaceURI()).isEqualTo("http://springframework.org/spring-ws");
	}

	@Test
	void getRootElementSaxSource() throws Exception {
		StringReader reader = new StringReader("<root xmlns='http://springframework.org/spring-ws'><child/></root>");

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();

		SAXSource saxSource = new SAXSource(xmlReader, new InputSource(reader));
		QName name = this.helper.getRootElement(saxSource);
		assertThat(name.getLocalPart()).isEqualTo("root");
		assertThat(name.getNamespaceURI()).isEqualTo("http://springframework.org/spring-ws");
	}

}