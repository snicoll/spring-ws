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

package org.springframework.ws.gradle.conventions.support;

import java.util.Objects;

import org.springframework.ws.gradle.conventions.support.Version.Parts;

/**
 * A {@link VersionUpgradePolicy} that matches candidate of the same minor version as the
 * current version.
 *
 * @author Stephane Nicoll
 */
class SameMinorUpgradePolicy implements VersionUpgradePolicy {

	@Override
	public boolean isCandidate(Version current, Version candidate) {
		if (current.equals(candidate)) { // LATEST
			return true;
		}
		Parts currentParts = current.getParts();
		Parts targetParts = candidate.getParts();
		if (currentParts != null && targetParts != null) {
			return Objects.equals(currentParts.major(), targetParts.major())
					&& Objects.equals(currentParts.minor(), targetParts.minor());
		}
		return false;
	}

}
