package org.tamrielrebuilt.topicoverlap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Topic {
	private final String name;
	private final Map<String, String> responses;
	private final Set<String> files;

	public Topic(String name) {
		this.name = name;
		this.responses = new HashMap<>();
		this.files = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	public void add(String id, String response) {
		responses.put(id, response);
	}

	public void addFile(String file) {
		files.add(file);
	}

	public void forOverlapping(Set<String> topics, boolean ignoreFullyContained, BiConsumer<String, Set<String>> consumer) {
		List<Map.Entry<Integer, String>> foundTopics = new ArrayList<>();
		for(String response : responses.values()) {
			getOverlapping(response, topics, ignoreFullyContained, foundTopics, overlapping -> consumer.accept(response, overlapping));
		}
	}

	private void getOverlapping(String response, Set<String> topics, boolean ignoreFullyContained,
			List<Map.Entry<Integer, String>> foundTopics, Consumer<Set<String>> consumer) {
		foundTopics.clear();
		for(String topic : topics) {
			if(topic.equals(name)) {
				continue;
			}
			int index = 0;
			do {
				index = response.indexOf(topic, index);
				if(index >= 0) {
					foundTopics.add(Map.entry(index, topic));
					index++;
				}
			} while(index >= 0);
		}
		if(foundTopics.size() > 1) {
			Set<String> overlapping = new HashSet<>();
			for(Map.Entry<Integer, String> match : foundTopics) {
				int start = match.getKey();
				int end = match.getValue().length() + start;
				for(Map.Entry<Integer, String> found : foundTopics) {
					if(match == found) {
						continue;
					}
					if(ignoreFullyContained && (found.getValue().contains(match.getValue()) || match.getValue().contains(found.getValue()))) {
						continue;
					}
					int fStart = found.getKey();
					int fEnd = found.getValue().length() + fStart;
					if(fStart >= start && fStart < end || fEnd > start && fEnd <= end) {
						overlapping.add(found.getValue());
						overlapping.add(match.getValue());
					}
				}
			}
			if(!overlapping.isEmpty()) {
				consumer.accept(overlapping);
			}
		}
	}

	@Override
	public String toString() {
		return name + " " + files;
	}
}
