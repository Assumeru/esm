package org.tamrielrebuilt.esm2yaml.esm.records;

import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.Record;
import org.tamrielrebuilt.esm2yaml.esm.jackson.JsonLockWriter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class Tes3WriterFactory extends AbstractRecordListenerFactory {

	public Tes3WriterFactory() {
		super("TES3");
	}

	@Override
	public CloseableRecordListener create(LockFileWriter writer, File directory, JsonFactory factory) {
		return new Listener(writer, new File(directory, "header.yaml"), factory);
	}

	private static class Listener implements CloseableRecordListener {
		private static final int FORMAT = Record.getValue("FORM");
		private static final int HEADER = Record.getValue("HEDR");
		private static final int MASTER = Record.getValue("MAST");
		private static final int LENGTH = Record.getValue("DATA");
		private final LockFileWriter writer;
		private final File file;
		private final JsonFactory factory;
		private JsonGenerator generator;

		public Listener(LockFileWriter writer, File file, JsonFactory factory) {
			this.writer = writer;
			this.file = file;
			this.factory = factory;
		}

		@Override
		public void onRecord(int type, int flags, int unknown) throws IOException {
			if(generator != null) {
				throw new IllegalStateException("Found more than 1 TES3 records");
			}
			writer.writeRecord(file);
			generator = factory.createGenerator(file, JsonEncoding.UTF8);
			generator.writeStartObject();
			JsonLockWriter.writeRecord(generator, flags, unknown);
		}

		@Override
		public void onSubrecord(int type, EsmInputStream input) throws IOException {
			if(type == FORMAT) {
				generator.writeNumberField("format", input.readLEInt());
			} else if(type == HEADER) {
				float version = input.readLEFloat();
				int fileType = input.readLEInt();
				String author = input.readName();
				String description = input.readString(256);
				int records = input.readLEInt();
				generator.writeNumberField("version", version);
				generator.writeNumberField("type", fileType);
				if(!author.isEmpty()) {
					generator.writeStringField("author", author);
				}
				if(!description.isEmpty()) {
					generator.writeStringField("description", author);
				}
				generator.writeNumberField("records", records);
			} else if(type == MASTER) {
				String name = input.readString();
				System.out.println(name + " ");
			} else if(type == LENGTH) {
				System.out.println(input.readLELong());
			} else {
				generator.writeStringField(Record.toString(type), input.readAll());
			}
		}

		@Override
		public void close() throws IOException {
			if(generator != null) {
				generator.writeEndObject();
				generator.close();
			}
		}
	}
}
