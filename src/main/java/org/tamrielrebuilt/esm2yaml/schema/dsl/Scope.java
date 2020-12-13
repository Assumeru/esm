package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class Scope {
	private final Map<String, Object> values;

	public Scope() {
		this.values = new HashMap<>();
	}

	@SuppressWarnings("rawtypes")
	private Object skipLists(Object current) {
		while(current instanceof List) {
			List list = (List) current;
			current = list.get(list.size() - 1);
		}
		return current;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void traverse(List<String> path, BiConsumer<String, Map<String, Object>> onEnd, boolean create) {
		int length = path.size();
		Object current = values;
		for(int i = 0; i < length; i++) {
			String key = path.get(i);
			if(i == length - 1) {
				onEnd.accept(key, (Map) current);
			} else {
				if(current instanceof Map) {
					Map map = (Map) current;
					Object next = map.get(key);
					if(next == null) {
						if(!create) {
							onEnd.accept(key, null);
							return;
						}
						next = new HashMap<>();
						map.put(key, next);
					}
					current = skipLists(next);
				} else {
					throw new IllegalStateException();
				}
			}
		}
	}

	public void set(List<String> path, Object value, boolean overwrite) {
		traverse(path, (key, map) -> {
			if(overwrite || !map.containsKey(key)) {
				map.put(key, value);
			}
		}, true);
	}

	public void set(String key, Object value) {
		values.put(key, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void push(List<String> path) {
		traverse(path, (key, map) -> {
			Object value = map.get(key);
			if(value == null) {
				value = new ArrayList<>();
				map.put(key, value);
			}
			((List) value).add(new HashMap<>());
		}, true);
	}

	public Object get(List<String> path) {
		Object[] output = new Object[1];
		traverse(path, (key, map) -> {
			if(map != null) {
				output[0] = map.get(key);
			}
		}, false);
		return output[0];
	}

	public Object get() {
		return values;
	}

	public void delete(List<String> path) {
		traverse(path, (key, map) -> {
			if(map != null) {
				map.remove(key);
			}
		}, false);
	}

	public void clear() {
		values.clear();
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
