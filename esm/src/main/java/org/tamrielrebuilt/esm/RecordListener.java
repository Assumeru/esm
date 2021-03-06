package org.tamrielrebuilt.esm;

import java.io.IOException;

public interface RecordListener {
	void onRecord(int type, int flags, int unknown) throws IOException;

	void onSubrecord(int type, EsmInputStream input) throws IOException;

	default void onRecordEnd() throws IOException {};
}
