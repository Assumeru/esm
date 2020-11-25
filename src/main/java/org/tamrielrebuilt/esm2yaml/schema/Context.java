package org.tamrielrebuilt.esm2yaml.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.RecordListener;

public class Context implements RecordListener {
	@Override
	public void onRecord(int type, int flags, int unknown) throws IOException {
		
	}

	@Override
	public void onSubrecord(int type, EsmInputStream input) throws IOException {
		input.skip(input.available());
	}

	public void writeStringField(String file, Field field, String value) throws IOException {
		System.out.println(file + " " + field + " " + value);
	}

	public void writeIntField(String file, Field field, int value) throws IOException {
		System.out.println(file + " " + field + " " + value);
	}

	public void writeFloatField(String file, Field field, float value) throws IOException {
		System.out.println(file + " " + field + " " + value);
	}

	public void writeLongField(String file, Field field, long value) throws IOException {
		System.out.println(file + " " + field + " " + value);
	}

	public void writeBooleanField(String file, Field field, boolean value) throws IOException {
		System.out.println(file + " " + field + " " + value);
	}

	public void write(String file, InputStream input) throws IOException {
		System.out.println(file);
		input.transferTo(System.out);
	}

	public void write(String file, Reader reader) throws IOException {
		System.out.println(file);
		StringWriter writer = new StringWriter();
		reader.transferTo(writer);
		System.out.println(writer.toString());
	}
}
