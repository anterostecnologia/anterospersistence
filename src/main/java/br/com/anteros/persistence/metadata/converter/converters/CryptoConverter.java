package br.com.anteros.persistence.metadata.converter.converters;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import br.com.anteros.core.utils.Base64;
import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

	private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
	private static final byte[] KEY = "AnterosSuperSecretKey@###10__".getBytes();

	@Override
	public String convertToDatabaseColumn(String ccNumber) {
		Key key = new SecretKeySpec(KEY, "AES");
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			return Base64.encodeBytes(c.doFinal(ccNumber.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		Key key = new SecretKeySpec(KEY, "AES");
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			return new String(c.doFinal(Base64.decode(dbData)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
