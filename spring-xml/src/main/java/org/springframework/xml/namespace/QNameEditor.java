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

package org.springframework.xml.namespace;

import java.beans.PropertyEditorSupport;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

/**
 * PropertyEditor for {@code javax.xml.namespace.QName}, to populate a property of type
 * QName from a String value.
 * <p>
 * Expects one of the following syntaxes:
 * <ul>
 * <li>{@code localPart}</li>
 * <li>{@code {namespace}localPart}</li>
 * <li>{@code {namespace}prefix:localPart}</li>
 * </ul>
 *
 * This resembles the {@code toString()} representation of {@code QName} itself, but
 * allows for prefixes to be specified as well.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see javax.xml.namespace.QName
 * @see javax.xml.namespace.QName#toString()
 * @see javax.xml.namespace.QName#valueOf(String)
 */
public class QNameEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(QNameUtils.parseQNameString(text));
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		if (value instanceof QName qName) {
			String prefix = qName.getPrefix();
			if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(prefix)) {
				return "{" + qName.getNamespaceURI() + "}" + prefix + ":" + qName.getLocalPart();
			}
			else if (StringUtils.hasLength(qName.getNamespaceURI())) {
				return "{" + qName.getNamespaceURI() + "}" + qName.getLocalPart();
			}
			else {
				return qName.getLocalPart();
			}
		}
		else {
			return "";
		}
	}

}
