package org.tamrielrebuilt.esm;

import java.io.IOException;

public final class RecordUtil {
	public static final int DELETED = getValue("DELE");

	private RecordUtil() {}

	public static <A extends Appendable> A appendTo(A appendable, int value) {
		try {
			appendable.append((char) ((value >> 24) & 0xFF));
			appendable.append((char) ((value >> 16) & 0xFF));
			appendable.append((char) ((value >> 8) & 0xFF));
			appendable.append((char) (value & 0xFF));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return appendable;
	}

	public static String toString(int value) {
		return appendTo(new StringBuilder(4), value).toString();
	}

	public static int getValue(CharSequence type) {
		int value = 0;
		for(int i = 0; i < 4; i++) {
			value <<= 8;
			value |= (type.charAt(i) & 0xFF);
		}
		return value;
	}
}
