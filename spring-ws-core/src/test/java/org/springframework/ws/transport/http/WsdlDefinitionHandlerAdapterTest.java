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

package org.springframework.ws.transport.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class WsdlDefinitionHandlerAdapterTest {

	private WsdlDefinitionHandlerAdapter adapter;

	private WsdlDefinition definitionMock;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	@BeforeEach
	public void setUp() throws Exception {

		adapter = new WsdlDefinitionHandlerAdapter();
		definitionMock = createMock(WsdlDefinition.class);
		adapter.afterPropertiesSet();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void handleGet() throws Exception {

		request.setMethod(HttpTransportConstants.METHOD_GET);
		String definition = "<definition xmlns='http://schemas.xmlsoap.org/wsdl/'/>";
		expect(definitionMock.getSource()).andReturn(new StringSource(definition));

		replay(definitionMock);

		adapter.handle(request, response, definitionMock);

		XmlAssert.assertThat(response.getContentAsString()).and(definition).ignoreWhitespace().areIdentical();

		verify(definitionMock);
	}

	@Test
	public void handleNonGet() throws Exception {

		request.setMethod(HttpTransportConstants.METHOD_POST);

		replay(definitionMock);

		adapter.handle(request, response, definitionMock);

		assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

		verify(definitionMock);
	}

	@Test
	public void transformLocations() throws Exception {

		adapter.setTransformLocations(true);
		request.setMethod(HttpTransportConstants.METHOD_GET);
		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(8080);
		request.setContextPath("/context");
		request.setServletPath("/service.wsdl");
		request.setPathInfo(null);
		request.setRequestURI("/context/service.wsdl");

		replay(definitionMock);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document result = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-input.wsdl"));
		adapter.transformLocations(result, request);
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-expected.wsdl"));

		XmlAssert.assertThat(result).and(expectedDocument).ignoreWhitespace().areIdentical();

		verify(definitionMock);
	}

	@Test
	public void transformLocationFullUrl() throws Exception {

		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(8080);
		request.setContextPath("/context");
		request.setPathInfo("/service.wsdl");
		request.setRequestURI("/context/service.wsdl");
		String oldLocation = "http://localhost:8080/context/service";

		String result = adapter.transformLocation(oldLocation, request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/context/service"));
	}

	@Test
	public void transformLocationEmptyContextFullUrl() throws Exception {

		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(8080);
		request.setContextPath("");
		request.setRequestURI("/service.wsdl");
		String oldLocation = "http://localhost:8080/service";

		String result = adapter.transformLocation(oldLocation, request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/service"));
	}

	@Test
	public void transformLocationRelativeUrl() throws Exception {

		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(8080);
		request.setContextPath("/context");
		request.setPathInfo("/service.wsdl");
		request.setRequestURI("/context/service.wsdl");
		String oldLocation = "/service";

		String result = adapter.transformLocation(oldLocation, request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/context/service"));
	}

	@Test
	public void transformLocationEmptyContextRelativeUrl() throws Exception {

		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(8080);
		request.setContextPath("");
		request.setRequestURI("/service.wsdl");
		String oldLocation = "/service";

		String result = adapter.transformLocation(oldLocation, request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/service"));
	}

	@Test
	public void handleSimpleWsdl11DefinitionWithoutTransformLocations() throws Exception {

		adapter.setTransformLocations(false);
		request.setMethod(HttpTransportConstants.METHOD_GET);
		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(8080);
		request.setContextPath("/context");
		request.setServletPath("/service.wsdl");
		request.setPathInfo(null);
		request.setRequestURI("/context/service.wsdl");

		SimpleWsdl11Definition definition = new SimpleWsdl11Definition(
				new ClassPathResource("echo-input.wsdl", getClass()));

		adapter.handle(request, response, definition);

		InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultingDocument = documentBuilder.parse(inputStream);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("echo-input.wsdl"));

		XmlAssert.assertThat(resultingDocument).and(expectedDocument).ignoreWhitespace().areIdentical();
	}

	@Test
	public void handleSimpleWsdl11DefinitionWithTransformLocation() throws Exception {

		adapter.setTransformLocations(true);
		adapter.setTransformSchemaLocations(true);

		request.setMethod(HttpTransportConstants.METHOD_GET);
		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(80);
		request.setContextPath("/context");
		request.setServletPath("/service.wsdl");
		request.setPathInfo(null);
		request.setRequestURI("/context/service.wsdl");

		SimpleWsdl11Definition definition = new SimpleWsdl11Definition(
				new ClassPathResource("echo-input.wsdl", getClass()));

		adapter.handle(request, response, definition);

		InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultingDocument = documentBuilder.parse(inputStream);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("echo-expected.wsdl"));

		XmlAssert.assertThat(resultingDocument).and(expectedDocument).ignoreWhitespace().areIdentical();
	}

	@Test
	public void handlesForwardedHeadersInRequest() {

		// given
		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(80);
		request.setContextPath("/context");
		request.setPathInfo("/service.wsdl");

		request.addHeader("X-Forwarded-Proto", "https");
		request.addHeader("X-Forwarded-Host", "loadbalancer.com");
		request.addHeader("X-Forwarded-Port", "8080");

		// when
		String result = adapter.transformLocation("/service", request);

		// then
		assertThat(URI.create("https://loadbalancer.com:8080/context/service")).isEqualTo(URI.create(result));
	}

	@Test
	public void handlesNoForwardedHeadersInRequest() {

		// given
		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(80);
		request.setContextPath("/context");
		request.setPathInfo("/service.wsdl");

		// when
		String result = adapter.transformLocation("/service", request);

		// then
		assertThat(URI.create("http://example.com:80/context/service")).isEqualTo(URI.create(result));
	}

}
