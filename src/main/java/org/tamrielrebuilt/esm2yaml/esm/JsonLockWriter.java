package org.tamrielrebuilt.esm2yaml.esm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.schema.LockFileWriter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class JsonLockWriter implements CloseableRecordListener, LockFileWriter {
	private final String directory;
	private final JsonFactory factory;
	private final StringBuilder stringBuffer;
	private final ByteArrayOutputStream byteBuffer;
	private final byte[] buffer;
	private JsonGenerator generator;

	public JsonLockWriter(File directory, JsonFactory factory) {
		this.directory = directory.getAbsolutePath();
		this.factory = factory;
		stringBuffer = new StringBuilder(4);
		byteBuffer = new ByteArrayOutputStream();
		buffer = new byte[1024];
	}

	public void open(String file) throws IOException {
		if(generator != null) {
			throw new IllegalStateException();
		}
		generator = factory.createGenerator(new File(directory, file), JsonEncoding.UTF8);
		generator.writeStartArray();
	}

	@Override
	public void writeRecord(File file) throws IOException {
		generator.writeStartObject();
		String path = file.getAbsolutePath();
		if(path.startsWith(directory)) {
			path = path.substring(directory.length());
		}
		generator.writeStringField("file", path);
		generator.writeEndObject();
	}

	public static void writeRecord(JsonGenerator generator, int flags, int unknown) throws IOException {
		if(flags != 0) {
			generator.writeNumberField("flags", flags);
		}
		if(unknown != 0) {
			generator.writeNumberField("unknown", flags);
		}
	}

	@Override
	public void onRecord(int type, int flags, int unknown) throws IOException {
		generator.writeStartObject();
		stringBuffer.setLength(0);
		RecordUtil.appendTo(stringBuffer, type);
		generator.writeStringField("type", stringBuffer.toString());
		writeRecord(generator, flags, unknown);
		generator.writeArrayFieldStart("subrecords");
	}

	@Override
	public void onSubrecord(int type, EsmInputStream input) throws IOException {
		generator.writeStartObject();
		stringBuffer.setLength(0);
		RecordUtil.appendTo(stringBuffer, type);
		generator.writeStringField("type", stringBuffer.toString());
		generator.writeStringField("data", input.readAll(byteBuffer, buffer));
		generator.writeEndObject();
	}

	@Override
	public void onRecordEnd() throws IOException {
		generator.writeEndArray();
		generator.writeEndObject();
	}

	@Override
	public void close() throws IOException {
		generator.writeEndArray();
		generator.close();
	}
}
