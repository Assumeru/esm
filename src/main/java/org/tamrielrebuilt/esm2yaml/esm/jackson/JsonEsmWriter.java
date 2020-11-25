package org.tamrielrebuilt.esm2yaml.esm.jackson;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;

public class JsonEsmWriter implements CloseableRecordListener {
	private final CloseableRecordListener listener;
	private final File directory;
	private final JsonFactory factory;
	private final JsonLockWriter lockFile;

	public JsonEsmWriter(File directory, JsonFactory factory, Function<JsonLockWriter, CloseableRecordListener> listenerFactory) throws IOException {
		if(directory.exists()) {
			//TODO throw new IllegalArgumentException(directory.getAbsolutePath() + " already exists");
		}
		directory.mkdirs();
		this.directory = directory;
		this.factory = factory;
		lockFile = new JsonLockWriter(directory, factory.createGenerator(new File(directory, "lock.yaml"), JsonEncoding.UTF8));
		listener = listenerFactory.apply(lockFile);
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
