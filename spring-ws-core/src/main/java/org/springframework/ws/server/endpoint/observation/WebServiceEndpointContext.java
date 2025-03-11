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

package org.springframework.ws.server.endpoint.observation;

import java.io.IOException;
import java.util.Iterator;

import io.micrometer.common.util.internal.logging.WarnThenDebugLogger;
import io.micrometer.observation.transport.RequestReplyReceiverContext;

import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;

/**
 * ObservationContext that describes how a WebService Endpoint is observed.
 *
 * @author Johan Kindgren
 */
public class WebServiceEndpointContext
		extends RequestReplyReceiverContext<HeadersAwareReceiverWebServiceConnection, TransportInputStream> {

	private static final WarnThenDebugLogger WARN_THEN_DEBUG_LOGGER = new WarnThenDebugLogger(
			WebServiceEndpointContext.class);

	private static final String UNKNOWN = "unknown";

	private String outcome = UNKNOWN;

	private String localPart = UNKNOWN;

	private String namespace = UNKNOWN;

	private String soapAction = UNKNOWN;

	private String path = UNKNOWN;

	private String pathInfo = null;

	public WebServiceEndpointContext(HeadersAwareReceiverWebServiceConnection connection) {
		super((carrier, key) -> {
			try {
				Iterator<String> headers = carrier.getRequestHeaders(key);
				if (headers.hasNext()) {
					return headers.next();
				}
			}
			catch (IOException ex) {
				WARN_THEN_DEBUG_LOGGER.log("Could not read key from carrier", ex);
			}
			return null;
		});
		setCarrier(connection);
	}

	public String getOutcome() {
		return this.outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public String getLocalPart() {
		return this.localPart;
	}

	public void setLocalPart(String localPart) {
		this.localPart = localPart;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getSoapAction() {
		return this.soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public String getPathInfo() {
		return this.pathInfo;
	}

}
