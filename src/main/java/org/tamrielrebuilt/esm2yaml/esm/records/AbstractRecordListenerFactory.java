package org.tamrielrebuilt.esm2yaml.esm.records;

import org.tamrielrebuilt.esm2yaml.esm.RecordUtil;

public abstract class AbstractRecordListenerFactory implements RecordListenerFactory {
	protected final int type;

	public AbstractRecordListenerFactory(int type) {
		this.type = type;
	}

	public AbstractRecordListenerFactory(CharSequence type) {
		this(RecordUtil.getValue(type));
	}

	@Override
	public int getType() {
		return type;
	}
}
