package org.tamrielrebuilt.esm2yaml.io;

import java.io.IOException;

@FunctionalInterface
public interface ThrowingConsumer<T> {
	void accept(T t) throws IOException;
}
