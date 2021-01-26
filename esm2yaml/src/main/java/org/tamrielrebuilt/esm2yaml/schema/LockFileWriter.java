package org.tamrielrebuilt.esm2yaml.schema;

import java.io.File;
import java.io.IOException;

public interface LockFileWriter {
	void writeRecord(File file) throws IOException;
}
