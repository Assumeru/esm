package org.tamrielrebuilt.topicoverlap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.tamrielrebuilt.esm.EsmReader;

public class TopicOverlapChecker {
	private final List<File> files;
	private final boolean ignoreFullyContained;

	public TopicOverlapChecker(List<File> files, boolean ignoreFullyContained) {
		this.files = files;
		this.ignoreFullyContained = ignoreFullyContained;
	}

	public static void main(String[] args) throws FileNotFoundException {
		if(args.length == 0) {
			System.out.println("Usage: <file.esm> [<file.esm>...] [--ignore-fully-contained]");
			System.exit(0);
		}
		List<File> files = new ArrayList<>(args.length);
		boolean ignoreFullyContained = false;
		for(String arg : args) {
			if("--ignore-fully-contained".equals(arg)) {
				ignoreFullyContained = true;
				continue;
			}
			File file = new File(arg);
			if(!file.exists()) {
				throw new FileNotFoundException(file.getAbsolutePath());
			}
			files.add(file);
		}
		new TopicOverlapChecker(files, ignoreFullyContained).run();
	}

	public void run() {
		TopicListener listener = new TopicListener();
		readTopics(listener);
		Set<String> topics = listener.getTopics();
		boolean[] hasOverlap = new boolean[1];
		listener.forTopics(topic -> {
			topic.forOverlapping(topics, ignoreFullyContained, (response, overlapping) -> {
				if(!hasOverlap[0]) {
					hasOverlap[0] = true;
					System.out.println(topic);
					System.out.println();
				}
				System.out.println(response);
				System.out.println(overlapping);
			});
			if(hasOverlap[0]) {
				System.out.println();
				System.out.println();
				hasOverlap[0] = false;
			}
		});
	}

	private void readTopics(TopicListener listener) {
		for(File file : files) {
			listener.setFile(file.getName());
			try(EsmReader reader = new EsmReader(file, StandardCharsets.ISO_8859_1)) {
				reader.read(listener);
			} catch(IOException e) {
				throw new RuntimeException("Failed to read " + file.getAbsolutePath(), e);
			}
		}
	}
}
