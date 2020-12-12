package org.tamrielrebuilt.esm2yaml.esm;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.tamrielrebuilt.esm2yaml.io.InputStreamView;

public class EsmReader implements Closeable {
	private final EsmInputStream input;

	public EsmReader(EsmInputStream input) {
		this.input = input;
	}

	public EsmReader(InputStream input, Charset charset) {
		this(new EsmInputStream(input, charset));
	}

	public EsmReader(File file, Charset charset) throws FileNotFoundException {
		this(new BufferedInputStream(new FileInputStream(file)), charset);
	}

	public void read(RecordListener listener) throws IOException {
		while(input.available() > 0) {
			readRecord(listener);
		}
	}

	private void readRecord(RecordListener listener) throws IOException {
		int type = input.readInt();
		long size = input.readLEInt() & 0xFFFFFFFFl;
		int unknown = input.readLEInt();
		int flags = input.readLEInt();
		EsmInputStream subrecords = new EsmInputStream(new InputStreamView(input, size), input.getCharset());
		listener.onRecord(type, flags, unknown);
		while(subrecords.available() > 0) {
			readSubrecord(subrecords, listener);
		}
		listener.onRecordEnd();
	}

	private void readSubrecord(EsmInputStream input, RecordListener listener) throws IOException {
		int type = input.readInt();
		long size = input.readLEInt() & 0xFFFFFFFFl;
		EsmInputStream subrecord = new EsmInputStream(new InputStreamView(input, size), input.getCharset());
		listener.onSubrecord(type, subrecord);
		if(subrecord.available() > 0) {
			throw new IllegalStateException(RecordUtil.appendTo(new StringBuilder("Failed to read complete subrecord "), type).toString());
		}
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
}
