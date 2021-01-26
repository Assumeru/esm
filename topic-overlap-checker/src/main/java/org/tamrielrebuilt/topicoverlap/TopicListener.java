package org.tamrielrebuilt.topicoverlap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.tamrielrebuilt.esm.EsmInputStream;
import org.tamrielrebuilt.esm.RecordListener;
import org.tamrielrebuilt.esm.RecordUtil;

public class TopicListener implements RecordListener {
	private static final int DIAL = RecordUtil.getValue("DIAL");
	private static final int INFO = RecordUtil.getValue("INFO");
	private static final int DATA = RecordUtil.getValue("DATA");
	private static final int NAME = RecordUtil.getValue("NAME");
	private static final int INAM = RecordUtil.getValue("INAM");
	private static final int TOPIC = 0;
	private static final int GREETING = 2;
	private final Map<String, Topic> topics;
	private final Map<String, Topic> greetings;
	private State state;
	private Topic current;
	private String name;
	private int type;
	private String id;
	private String file;

	public TopicListener() {
		topics = new HashMap<>();
		greetings = new HashMap<>();
		state = State.NONE;
	}

	public void forTopics(Consumer<Topic> consumer) {
		topics.values().forEach(consumer);
		greetings.values().forEach(consumer);
	}

	public Set<String> getTopics() {
		return topics.keySet();
	}

	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public void onRecord(int record, int flags, int unknown) throws IOException {
		if(record == DIAL) {
			state = State.DIAL;
			name = null;
			type = -1;
		} else if(record == INFO) {
			state = State.RESPONSE;
			name = null;
			id = null;
		} else {
			state = State.NONE;
		}
	}

	@Override
	public void onRecordEnd() throws IOException {
		if(state == State.DIAL) {
			current = null;
			if(name != null && !name.isEmpty()) {
				Map<String, Topic> map = null;
				if(type == GREETING) {
					map = greetings;
				} else if(type == TOPIC) {
					map = topics;
				} else {
					return;
				}
				current = map.get(name);
				if(current == null) {
					current = new Topic(name);
					map.put(name, current);
				}
				current.addFile(file);
			}
		} else if(state == State.RESPONSE && current != null && name != null && id != null) {
			current.add(id, name);
		}
	}

	@Override
	public void onSubrecord(int sub, EsmInputStream input) throws IOException {
		if(state == State.DIAL) {
			if(sub == NAME) {
				name = input.readString().toLowerCase(Locale.US);
			} else if(sub == DATA) {
				type = input.read();
			}
		} else if(state == State.RESPONSE && current != null) {
			if(sub == NAME) {
				name = input.readString().toLowerCase(Locale.US);
			} else if(sub == INAM) {
				id = input.readString();
			}
		}
		input.skip(input.available());
	}

	private enum State {
		NONE, DIAL, RESPONSE
	}
}
