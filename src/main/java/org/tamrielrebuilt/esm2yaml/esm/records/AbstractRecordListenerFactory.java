package org.tamrielrebuilt.esm2yaml.esm.records;

import org.tamrielrebuilt.esm2yaml.esm.Record;

public abstract class AbstractRecordListenerFactory implements RecordListenerFactory {
	protected final int type;

	public AbstractRecordListenerFactory(int type) {
		this.type = type;
	}

	public AbstractRecordListenerFactory(CharSequence type) {
		this(Record.getValue(type));
	}

	@Override
	public int getType() {
		return type;
	}
}
