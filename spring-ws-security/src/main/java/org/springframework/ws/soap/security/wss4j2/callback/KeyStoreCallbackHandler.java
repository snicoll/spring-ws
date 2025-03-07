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

package org.springframework.ws.soap.security.wss4j2.callback;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.soap.security.support.KeyStoreUtils;

/**
 * Callback handler that uses Java Security {@code KeyStore}s to handle cryptographic
 * callbacks. Allows for specific key stores to be set for various cryptographic
 * operations.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @see org.springframework.ws.soap.security.support.KeyStoreFactoryBean
 * @since 2.3.0
 */
public class KeyStoreCallbackHandler extends AbstractWsPasswordCallbackHandler implements InitializingBean {

	private String privateKeyPassword;

	private char[] symmetricKeyPassword;

	private KeyStore keyStore;

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#DECRYPT} usage.
	 * <p>
	 * This method is invoked when WSS4J needs a password to get the private key of the
	 * {@link WSPasswordCallback#getIdentifier() identifier} (username) from the keystore.
	 * WSS4J uses this private key to decrypt the session (symmetric) key. Because the
	 * encryption method uses the public key to encrypt the session key it needs no
	 * password (a public key is usually not protected by a password).
	 * <p>
	 * Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleDecrypt(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		callback.setPassword(this.privateKeyPassword);
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#SECRET_KEY} usage.
	 * <p>
	 * Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleSecretKey(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		String id = callback.getIdentifier();
		Key key;

		try {
			key = this.keyStore.getKey(id, this.symmetricKeyPassword != null ? this.symmetricKeyPassword
					: this.privateKeyPassword.toCharArray());
		}
		catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			throw new IOException("Could not get key", e);
		}

		callback.setKey(key.getEncoded());
	}

	/** Sets the key store to use if a symmetric key name is embedded. */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * Sets the password used to retrieve private keys from the keystore. This property is
	 * required for decryption based on private keys, and signing.
	 */
	public void setPrivateKeyPassword(String privateKeyPassword) {
		if (privateKeyPassword != null) {
			this.privateKeyPassword = privateKeyPassword;
		}
	}

	/**
	 * Sets the password used to retrieve keys from the symmetric keystore. If this
	 * property is not set, it defaults to the private key password.
	 * @see #setPrivateKeyPassword(String)
	 */
	public void setSymmetricKeyPassword(String symmetricKeyPassword) {
		if (symmetricKeyPassword != null) {
			this.symmetricKeyPassword = symmetricKeyPassword.toCharArray();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.keyStore == null) {
			loadDefaultKeyStore();
		}
		if (this.symmetricKeyPassword == null) {
			this.symmetricKeyPassword = this.privateKeyPassword.toCharArray();
		}
	}

	/**
	 * Loads the key store indicated by system properties. Delegates to
	 * {@link KeyStoreUtils#loadDefaultKeyStore()}.
	 */
	protected void loadDefaultKeyStore() {
		try {
			this.keyStore = KeyStoreUtils.loadDefaultKeyStore();
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Loaded default key store");
			}
		}
		catch (Exception ex) {
			this.logger.warn("Could not open default key store", ex);
		}
	}

}
