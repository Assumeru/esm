package org.tamrielrebuilt.esm2yaml.esm;

import java.io.IOException;

public interface RecordListener {
	void onRecord(int type, int flags, int unknown) throws IOException;

	void onSubrecord(int type, EsmInputStream input) throws IOException;
}
