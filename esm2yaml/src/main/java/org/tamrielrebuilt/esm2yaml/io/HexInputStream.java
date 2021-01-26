package org.tamrielrebuilt.esm2yaml.io;

import java.io.IOException;
import java.io.InputStream;

public class HexInputStream extends InputStream {
	private static final int[] DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	private final InputStream input;
	private int buffer;

	public HexInputStream(InputStream input) {
		this.input = input;
	}

	@Override
	public int read() throws IOException {
		if(buffer > 0) {
			int r = buffer;
			buffer = 0;
			return r;
		}
		int r = input.read();
		if(r < 0) {
			return r;
		}
		buffer = DIGITS[r & 0xF];
		return DIGITS[(r >> 4) & 0xF];
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(len > 0) {
			int read = 0;
			if(buffer > 0) {
				b[off++] = (byte) buffer;
				buffer = 0;
				len--;
				read++;
			}
			int half = len / 2;
			if((len & 1) == 1) {
				half++;
			}
			if(half > 0) {
				int r = input.read(b, off, half);
				read += r * 2;
				int index = off + r - 1;
				int dest = off + r * 2 - 1;
				if(read > len) {
					buffer = DIGITS[b[index] & 0xF];
					read--;
					b[--dest] = (byte) DIGITS[(b[index--] >> 4) & 0xF];
					dest--;
				}
				for(; index >= off; index--) {
					b[dest--] = (byte) DIGITS[b[index] & 0xF];
					b[dest--] = (byte) DIGITS[(b[index] >> 4) & 0xF];
				}
			}
			return read;
		}
		return 0;
	}

	@Override
	public long skip(long n) throws IOException {
		if(n > 0) {
			long skipped = 0;
			if(buffer > 0) {
				buffer = 0;
				skipped++;
				n--;
			}
			long half = n / 2;
			if(half != 0) {
				long s = input.skip(half);
				if(s >= 0) {
					skipped += s;
					n -= s * 2;
				} else if(skipped == 0) {
					return s;
				}
			}
			if((n & 1) == 1 && read() >= 0) {
				skipped++;
			}
			return skipped;
		}
		return 0;
	}

	@Override
	public int available() throws IOException {
		if(buffer > 0) {
			return 1;
		}
		return input.available();
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
}
