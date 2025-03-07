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

package org.springframework.ws.pox.dom;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class DomPoxMessageTest {

	private DomPoxMessage message;

	private Transformer transformer;

	@BeforeEach
	public void setUp() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		this.transformer = transformerFactory.newTransformer();
		this.message = new DomPoxMessage(document, this.transformer, DomPoxMessageFactory.DEFAULT_CONTENT_TYPE);
	}

	@Test
	public void testGetPayload() throws Exception {

		String content = "<root xmlns='http://www.springframework.org/spring-ws'>" + "<child/></root>";
		StringSource source = new StringSource(content);
		this.transformer.transform(source, this.message.getPayloadResult());
		StringResult stringResult = new StringResult();
		this.transformer.transform(this.message.getPayloadSource(), stringResult);

		assertThat(stringResult.toString()).and(content).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testGetPayloadResultTwice() throws Exception {

		String content = "<element xmlns=\"http://www.springframework.org/spring-ws\" />";
		this.transformer.transform(new StringSource(content), this.message.getPayloadResult());
		this.transformer.transform(new StringSource(content), this.message.getPayloadResult());
		StringResult stringResult = new StringResult();
		this.transformer.transform(this.message.getPayloadSource(), stringResult);

		assertThat(stringResult.toString()).and(content).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testWriteTo() throws Exception {

		String content = "<root xmlns='http://www.springframework.org/spring-ws'>" + "<child/></root>";
		StringSource source = new StringSource(content);
		this.transformer.transform(source, this.message.getPayloadResult());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		this.message.writeTo(os);

		assertThat(os.toString(StandardCharsets.UTF_8)).and(content).ignoreWhitespace().areIdentical();
	}

}
