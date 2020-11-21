package org.tamrielrebuilt.esm2yaml.esm.jackson;

import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.TypedRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.records.RecordListenerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;

public class JsonEsmWriter implements CloseableRecordListener {
	private final TypedRecordListener listener;
	private final File directory;
	private final JsonFactory factory;
	private final JsonLockWriter lockFile;

	public JsonEsmWriter(File directory, JsonFactory factory) throws IOException {
		if(directory.exists()) {
			//TODO throw new IllegalArgumentException(directory.getAbsolutePath() + " already exists");
		}
		directory.mkdirs();
		this.directory = directory;
		this.factory = factory;
		lockFile = new JsonLockWriter(directory, factory.createGenerator(new File(directory, "lock.yaml"), JsonEncoding.UTF8));
		listener = new TypedRecordListener(lockFile);
	}

	public void register(RecordListenerFactory factory) {
		listener.register(factory.getType(), factory.create(lockFile, directory, this.factory));
	}

	@Override
	public void onRecord(int type, int flags, int unknown) throws IOException {
		listener.onRecord(type, flags, unknown);
	}

	@Override
	public void onSubrecord(int type, EsmInputStream input) throws IOException {
		listener.onSubrecord(type, input);
	}

	@Override
	public void close() throws IOException {
		listener.close();
	}
}
