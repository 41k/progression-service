package root.infrastructure.persistence.converter;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;

public abstract class ZipConverter<T> implements AttributeConverter<T, byte[]> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public byte[] convertToDatabaseColumn(T attribute) {
		try (
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream)
		) {
			String attributeAsString = OBJECT_MAPPER.writeValueAsString(attribute);
			byte[] encodedBytes = UTF_8.encode(attributeAsString).array();
			deflaterOutputStream.write(encodedBytes);
			deflaterOutputStream.finish();
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Failed to compress and encode data.", e);
		}
	}

	@Override
	public T convertToEntityAttribute(byte[] data) {
		try (
				ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
				InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream)
		) {
			byte[] unzippedBytes = inflaterInputStream.readAllBytes();
			ByteBuffer byteBuffer = ByteBuffer.wrap(unzippedBytes);
			String attributeAsString = UTF_8.decode(byteBuffer).toString().trim();
			return OBJECT_MAPPER.readValue(attributeAsString, getTypeReference());
		} catch (Exception e) {
			throw new RuntimeException("Failed to decompress and decode data.", e);
		}
	}

	protected abstract TypeReference<T> getTypeReference();
}
