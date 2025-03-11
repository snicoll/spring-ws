/*
 * Copyright 2005-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ws.client.core.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.util.internal.logging.WarnThenDebugLogger;
import io.micrometer.observation.transport.RequestReplySenderContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;

import java.io.IOException;
/**
 * ObservationContext used to instrument a WebServiceTemplate operation.
 * @author Johan Kindgren
 */
public class WebServiceTemplateObservationContext extends RequestReplySenderContext<HeadersAwareSenderWebServiceConnection, TransportInputStream> {

    private static final WarnThenDebugLogger WARN_THEN_DEBUG_LOGGER = new WarnThenDebugLogger(WebServiceTemplateObservationContext.class);

    public static final String UNKNOWN = "unknown";
    private String outcome = UNKNOWN;
    private String localPart = UNKNOWN;
    private String namespace = UNKNOWN;
    private String host = UNKNOWN;
    private String soapAction = KeyValue.NONE_VALUE;
    private String path = null;

    public WebServiceTemplateObservationContext(HeadersAwareSenderWebServiceConnection connection) {
        super((carrier, key, value) -> {

            if (carrier != null) {
                try {
                    carrier.addRequestHeader(key, value);
                } catch (IOException e) {
                    WARN_THEN_DEBUG_LOGGER.log("Could not add key to carrier", e);
                }
            }
        });
        setCarrier(connection);
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getLocalPart() {
        return localPart;
    }

    public void setLocalPart(String localPart) {
        this.localPart = localPart;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
