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

import java.time.Duration;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Little helper to build a changelog from the tickets of a particular milestone.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class ChangelogCreator {

	private static final int MILESTONE_ID = 91;

	private static final String URI_TEMPLATE = "https://api.github.com/repos/spring-projects/spring-ws/issues?milestone={id}&state=closed";

	public static void main(String... args) {

		/*
		 * If you run into github rate limiting issues, you can always use a Github
		 * Personal Token by adding {@code .header(HttpHeaders.AUTHORIZATION,
		 * "token your-github-token")} to the webClient call.
		 */

		WebClient webClient = WebClient.create();

		try {
			HttpEntity<String> response = webClient //
				.get()
				.uri(URI_TEMPLATE, MILESTONE_ID) //
				// .header(HttpHeaders.AUTHORIZATION, "token <plugin in a personal token
				// and uncomment>") //
				.exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)) //
				.block(Duration.ofSeconds(10));

			boolean keepChecking = true;
			boolean printHeader = true;

			while (keepChecking) {

				readPage(response.getBody(), printHeader);
				printHeader = false;

				List<String> linksInHeader = response.getHeaders().get(HttpHeaders.LINK);
				Links links = linksInHeader == null ? Links.NONE : Links.parse(linksInHeader.get(0));

				if (links.getLink(IanaLinkRelations.NEXT).isPresent()) {

					response = webClient //
						.get()
						.uri(links.getRequiredLink(IanaLinkRelations.NEXT).expand().getHref()) //
						.exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)) //
						.block(Duration.ofSeconds(10));

				}
				else {
					keepChecking = false;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Couldn't retrieve MILESTONE " + MILESTONE_ID);
		}
	}

	private static void readPage(String content, boolean header) {

		JsonPath titlePath = JsonPath.compile("$[*].title");
		JsonPath idPath = JsonPath.compile("$[*].number");

		JSONArray titles = titlePath.read(content);
		Iterator<Object> ids = ((JSONArray) idPath.read(content)).iterator();

		if (header) {
			System.out.println("Changes in version " + JsonPath.read(content, "$[0].milestone.title") + " ("
					+ LocalDate.now() + ")");
			System.out.println("----------------------------------------");
		}

		for (Object title : titles) {

			String format = String.format("- #%s - %s", ids.next(), title.toString().replaceAll("`", ""));
			System.out.println(format.endsWith(".") ? format : format.concat("."));
		}
	}

}
