package org.tamrielrebuilt.esm2yaml.schema;

import java.util.Collections;
import java.util.List;

public class Field {
	private final List<String> path;

	public Field(String name) {
		this(Collections.singletonList(name));
	}

	public Field(List<String> path) {
		this.path = path;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String segment : path) {
			if(sb.length() > 0) {
				sb.append('.');
			}
			sb.append(segment);
		}
		return sb.toString();
	}
}
