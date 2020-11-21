package org.tamrielrebuilt.esm2yaml.esm.records;

import java.io.File;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;

import com.fasterxml.jackson.core.JsonFactory;

public interface RecordListenerFactory {
	CloseableRecordListener create(LockFileWriter writer, File directory, JsonFactory factory);

	int getType();
}
