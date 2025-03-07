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

package org.springframework.ws.config;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
public class WsdlBeanDefinitionParserTest {

	private ApplicationContext applicationContext;

	@BeforeEach
	public void setUp() {
		this.applicationContext = new ClassPathXmlApplicationContext("wsdlBeanDefinitionParserTest.xml", getClass());
	}

	@Test
	public void staticWsdl() {

		Map<String, SimpleWsdl11Definition> result = this.applicationContext
			.getBeansOfType(SimpleWsdl11Definition.class);

		assertThat(result).isNotEmpty();

		String beanName = result.keySet().iterator().next();

		assertThat(beanName).isEqualTo("simple");
	}

	@Test
	public void dynamicWsdl() {

		Map<String, ?> result = this.applicationContext.getBeansOfType(DefaultWsdl11Definition.class);

		assertThat(result).isNotEmpty();

		result = this.applicationContext.getBeansOfType(CommonsXsdSchemaCollection.class);

		assertThat(result).isNotEmpty();
	}

}
