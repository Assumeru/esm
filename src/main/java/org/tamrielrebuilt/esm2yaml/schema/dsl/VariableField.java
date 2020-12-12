package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tamrielrebuilt.esm2yaml.schema.Context;

public class VariableField {
	private final List<Resolvable> path;

	private VariableField(List<Resolvable> path) {
		this.path = path;
	}

	public List<String> getPath(Context context) {
		return path.stream().flatMap(s -> s.resolve(context)).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Resolvable segment : path) {
			if(sb.length() > 0) {
				sb.append('.');
			}
			sb.append(segment);
		}
		return sb.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<Resolvable> resolvables = new ArrayList<>();

		public void append(String segment) {
			resolvables.add(c -> Stream.of(segment));
		}

		public void append(VariableField field, boolean local) {
			resolvables.add(new ResolvableVariable(field, local));
		}

		public VariableField build() {
			return new VariableField(resolvables);
		}
	}
}
