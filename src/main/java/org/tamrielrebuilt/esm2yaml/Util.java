package org.tamrielrebuilt.esm2yaml;

public final class Util {
	private Util() {}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
		throw (T) t;
	}
}
