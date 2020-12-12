package org.tamrielrebuilt.esm2yaml.esm.jackson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.RecordUtil;
import org.tamrielrebuilt.esm2yaml.esm.records.LockFileWriter;

import com.fasterxml.jackson.core.JsonGenerator;

public class JsonLockWriter implements CloseableRecordListener, LockFileWriter {
	private final String directory;
	private final JsonGenerator generator;
	private final StringBuilder stringBuffer;
	private final ByteArrayOutputStream byteBuffer;
	private final byte[] buffer;
	private boolean recordOpen;
	private boolean subrecordOpen;

	public JsonLockWriter(File directory, JsonGenerator generator) throws IOException {
		this.directory = directory.getAbsolutePath();
		this.generator = generator;
		stringBuffer = new StringBuilder(4);
		byteBuffer = new ByteArrayOutputStream();
		buffer = new byte[1024];
		generator.writeStartArray();
	}

	@Override
	public void writeRecord(File file) throws IOException {
		onRecordEnd();
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
		recordOpen = true;
		stringBuffer.setLength(0);
		RecordUtil.appendTo(stringBuffer, type);
		generator.writeStringField("type", stringBuffer.toString());
		writeRecord(generator, flags, unknown);
		generator.writeArrayFieldStart("subrecords");
		subrecordOpen = true;
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
		if(recordOpen) {
			if(subrecordOpen) {
				generator.writeEndArray();
				subrecordOpen = false;
			}
			generator.writeEndObject();
			recordOpen = false;
		}
	}

	@Override
	public void close() throws IOException {
		onRecordEnd();
		generator.writeEndArray();
		generator.close();
	}
}
