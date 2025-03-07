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

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.FreePortScanner;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractHttpWebServiceMessageSenderIntegrationTest<T extends AbstractHttpWebServiceMessageSender> {

	private Server jettyServer;

	private static final String REQUEST_HEADER_NAME = "RequestHeader";

	private static final String REQUEST_HEADER_VALUE = "RequestHeaderValue";

	private static final String RESPONSE_HEADER_NAME = "ResponseHeader";

	private static final String RESPONSE_HEADER_VALUE = "ResponseHeaderValue";

	private static final String REQUEST = "<Request xmlns='http://springframework.org/spring-ws/' />";

	private static final String SOAP_REQUEST = "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'><SOAP-ENV:Header/><SOAP-ENV:Body>"
			+ REQUEST + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	private static final String RESPONSE = "<Response  xmlns='http://springframework.org/spring-ws/' />";

	private static final String SOAP_RESPONSE = "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'><SOAP-ENV:Header/><SOAP-ENV:Body>"
			+ RESPONSE + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	private ServletContextHandler jettyContext;

	private MessageFactory saajMessageFactory;

	private TransformerFactory transformerFactory;

	private WebServiceMessageFactory messageFactory;

	protected T messageSender;

	protected URI connectionUri;

	@BeforeEach
	public final void setUp() throws Exception {

		int port = FreePortScanner.getFreePort();
		this.connectionUri = new URI("http", null, "localhost", port, null, null, null);

		this.jettyServer = new Server(port);
		Connector connector = new ServerConnector(this.jettyServer);
		this.jettyServer.addConnector(connector);

		this.jettyContext = new ServletContextHandler();
		this.jettyContext.setServer(this.jettyServer);

		this.messageSender = createMessageSender();

		if (this.messageSender instanceof InitializingBean) {
			((InitializingBean) this.messageSender).afterPropertiesSet();
		}

		this.saajMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		this.messageFactory = new SaajSoapMessageFactory(this.saajMessageFactory);
		this.transformerFactory = TransformerFactoryUtils.newInstance();
	}

	protected abstract T createMessageSender();

	@AfterEach
	public final void tearDown() throws Exception {

		if (this.jettyServer.isRunning()) {
			this.jettyServer.stop();
		}
	}

	@Test
	public void testSupports() {
		assertThat(this.messageSender.supports(this.connectionUri)).isTrue();
	}

	@Test
	public void testSendAndReceiveResponse() throws Exception {

		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveNoResponse() throws Exception {
		validateNonResponse(new MyServlet());
	}

	@Test
	public void testSendAndReceiveNoResponseAccepted() throws Exception {

		MyServlet servlet = new MyServlet();
		servlet.setResponseStatus(HttpServletResponse.SC_ACCEPTED);
		validateNonResponse(servlet);
	}

	@Test
	public void testSendAndReceiveCompressed() throws Exception {

		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		servlet.setGzip(true);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveInvalidContentSize() throws Exception {

		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		servlet.setContentLength(-1);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveCompressedInvalidContentSize() throws Exception {

		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		servlet.setGzip(true);
		servlet.setContentLength(-1);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveFault() throws Exception {

		MyServlet servlet = new MyServlet();
		servlet.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		servlet.setResponse(true);

		this.jettyContext.addServlet(new ServletHolder(servlet), "/");
		this.jettyServer.setHandler(this.jettyContext);
		this.jettyServer.start();

		try (FaultAwareWebServiceConnection connection = (FaultAwareWebServiceConnection) this.messageSender
			.createConnection(this.connectionUri)) {
			SOAPMessage request = createRequest();
			connection.send(new SaajSoapMessage(request));
			connection.receive(this.messageFactory);

			assertThat(connection.hasFault()).isTrue();
		}
	}

	private void validateResponse(Servlet servlet) throws Exception {

		this.jettyContext.addServlet(new ServletHolder(servlet), "/");
		this.jettyServer.setHandler(this.jettyContext);
		this.jettyServer.start();

		try (FaultAwareWebServiceConnection connection = (FaultAwareWebServiceConnection) this.messageSender
			.createConnection(this.connectionUri)) {
			SOAPMessage request = createRequest();
			connection.send(new SaajSoapMessage(request));
			SaajSoapMessage response = (SaajSoapMessage) connection.receive(this.messageFactory);

			assertThat(response).isNotNull();
			assertThat(connection.hasFault()).isFalse();

			SOAPMessage saajResponse = response.getSaajMessage();
			String[] headerValues = saajResponse.getMimeHeaders().getHeader(RESPONSE_HEADER_NAME);

			assertThat(headerValues).isNotNull();
			assertThat(headerValues).containsExactly(RESPONSE_HEADER_VALUE);

			StringResult result = new StringResult();
			Transformer transformer = this.transformerFactory.newTransformer();
			transformer.transform(response.getPayloadSource(), result);

			XmlAssert.assertThat(result.toString()).and(RESPONSE).ignoreWhitespace().areIdentical();
		}
	}

	private void validateNonResponse(Servlet servlet) throws Exception {

		this.jettyContext.addServlet(new ServletHolder(servlet), "/");
		this.jettyServer.setHandler(this.jettyContext);
		this.jettyServer.start();

		try (WebServiceConnection connection = this.messageSender.createConnection(this.connectionUri)) {
			SOAPMessage request = createRequest();
			connection.send(new SaajSoapMessage(request));
			WebServiceMessage response = connection.receive(this.messageFactory);

			assertThat(response).isNull();
		}
	}

	private SOAPMessage createRequest() throws TransformerException, SOAPException {

		SOAPMessage request = this.saajMessageFactory.createMessage();
		MimeHeaders mimeHeaders = request.getMimeHeaders();
		mimeHeaders.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
		Transformer transformer = this.transformerFactory.newTransformer();
		transformer.transform(new StringSource(REQUEST), new DOMResult(request.getSOAPBody()));

		return request;
	}

	@SuppressWarnings("serial")
	public static class MyServlet extends HttpServlet {

		private int responseStatus = HttpServletResponse.SC_OK;

		private Integer contentLength;

		private boolean response;

		private boolean gzip;

		public void setResponseStatus(int responseStatus) {
			this.responseStatus = responseStatus;
		}

		public void setContentLength(int contentLength) {
			this.contentLength = contentLength;
		}

		public void setResponse(boolean response) {
			this.response = response;
		}

		public void setGzip(boolean gzip) {
			this.gzip = gzip;
		}

		@Override
		protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
				throws ServletException {
			try {
				assertThat(httpServletRequest.getHeader(REQUEST_HEADER_NAME)).isEqualTo(REQUEST_HEADER_VALUE);

				String receivedRequest = new String(FileCopyUtils.copyToByteArray(httpServletRequest.getInputStream()),
						StandardCharsets.UTF_8);

				XmlAssert.assertThat(receivedRequest).and(SOAP_REQUEST).ignoreWhitespace().areIdentical();

				if (this.gzip) {
					assertThat(httpServletRequest.getHeader("Accept-Encoding")).isEqualTo("gzip");
				}

				httpServletResponse.setStatus(this.responseStatus);

				if (this.response) {

					httpServletResponse.addHeader("content-type", "text/xml");

					if (this.contentLength != null) {
						httpServletResponse.setContentLength(this.contentLength);
					}

					if (this.gzip) {
						httpServletResponse.addHeader("Content-Encoding", "gzip");
					}

					httpServletResponse.setHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
					OutputStream os;

					if (this.gzip) {
						os = new GZIPOutputStream(httpServletResponse.getOutputStream());
					}
					else {
						os = httpServletResponse.getOutputStream();
					}

					FileCopyUtils.copy(SOAP_RESPONSE.getBytes(StandardCharsets.UTF_8), os);
				}
			}
			catch (Exception ex) {
				throw new ServletException(ex);
			}
		}

	}

}
