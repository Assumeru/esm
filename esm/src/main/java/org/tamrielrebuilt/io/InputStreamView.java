package org.tamrielrebuilt.io;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamView extends InputStream {
	private final InputStream input;
	private long bytesLeft;

	public InputStreamView(InputStream input, long maxLength) {
		this.input = input;
		this.bytesLeft = maxLength;
	}

	@Override
	public int read() throws IOException {
		if(bytesLeft < 1) {
			return -1;
		}
		int r = input.read();
		if(r >= 0) {
			bytesLeft--;
		}
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(bytesLeft < 1) {
			return -1;
		}
		int r = input.read(b, off, (int) Math.min(bytesLeft, len));
		if(r > 0) {
			bytesLeft -= r;
		}
		return r;
	}

	@Override
	public long skip(long n) throws IOException {
		if(bytesLeft < 1) {
			return 0;
		}
		long skipped = input.skip(Math.min(n, bytesLeft));
		bytesLeft -= skipped;
		return skipped;
	}

	@Override
	public int available() throws IOException {
		return (int) Math.min(bytesLeft, Integer.MAX_VALUE);
	}

	@Override
	public void close() {
	}
}
