package org.tamrielrebuilt.esm2yaml.esm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EsmInputStream extends InputStream {
	private final InputStream input;
	private final Charset charset;
	private final byte[] buffer;

	public EsmInputStream(InputStream input, Charset charset) {
		this.input = input;
		this.charset = charset;
		buffer = new byte[32];
	}

	public Charset getCharset() {
		return charset;
	}

	@Override
	public int read() throws IOException {
		return input.read();
	}

	@Override
	public long skip(long n) throws IOException {
		long done = 0;
		while(done < n) {
			long skipped = input.skip(n - done);
			if(skipped > 0) {
				done += skipped;
			} else {
				break;
			}
		}
		return done;
	}

	@Override
	public int available() throws IOException {
		return input.available();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return input.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return input.read(b, off, len);
	}

	public int readLEInt() throws IOException {
		if(read(buffer, 0, 4) != 4) {
			throw new IOException("Failed to read int");
		}
		return ((buffer[3] & 0xFF) << 24) | ((buffer[2] & 0xFF) << 16) | ((buffer[1] & 0xFF) << 8) | (buffer[0] & 0xFF);
	}

	public int readInt() throws IOException {
		if(read(buffer, 0, 4) != 4) {
			throw new IOException("Failed to read int");
		}
		return ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
	}

	public long readLELong() throws IOException {
		if(read(buffer, 0, 8) != 8) {
			throw new IOException("Failed to read long");
		}
		return ((buffer[7] & 0xFFl) << 56) | ((buffer[6] & 0xFFl) << 48) | ((buffer[5] & 0xFFl) << 40) | ((buffer[4] & 0xFFl) << 32)
				| ((buffer[3] & 0xFFl) << 24) | ((buffer[2] & 0xFFl) << 16) | ((buffer[1] & 0xFFl) << 8) | (buffer[0] & 0xFFl);
	}

	public long readLong() throws IOException {
		if(read(buffer, 0, 8) != 8) {
			throw new IOException("Failed to read long");
		}
		return ((buffer[0] & 0xFFl) << 56) | ((buffer[1] & 0xFFl) << 48) | ((buffer[2] & 0xFFl) << 40) | ((buffer[3] & 0xFFl) << 32)
				| ((buffer[4] & 0xFFl) << 24) | ((buffer[5] & 0xFFl) << 16) | ((buffer[6] & 0xFFl) << 8) | (buffer[7] & 0xFFl);
	}

	public short readLEShort() throws IOException {
		if(read(buffer, 0, 2) != 2) {
			throw new IOException("Failed to read short");
		}
		return (short) (((buffer[1] & 0xFF) << 8) | (buffer[0] & 0xFF));
	}

	public short readShort() throws IOException {
		if(read(buffer, 0, 2) != 2) {
			throw new IOException("Failed to read short");
		}
		return (short) (((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF));
	}

	public float readLEFloat() throws IOException {
		return Float.intBitsToFloat(readLEInt());
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public String readString(int length) throws IOException {
		if(length == 0) {
			return "";
		} else if(length <= buffer.length) {
			return readString(buffer, length);
		}
		return readString(new byte[length], length);
	}

	private String readString(byte[] buffer, int length) throws IOException {
		if(read(buffer, 0, length) != length) {
			throw new IOException("Failed to read string");
		}
		int pos = 0;
		while(buffer[pos] != 0 && pos < length) {
			pos++;
		}
		return new String(buffer, 0, pos, charset);
	}

	public String readName() throws IOException {
		return readString(buffer, 32);
	}

	public String readString() throws IOException {
		return readString(available());
	}

	public String readAll() throws IOException {
		return readAll(new ByteArrayOutputStream(), new byte[1024]);
	}

	public String readAll(ByteArrayOutputStream byteBuffer, byte[] buffer) throws IOException {
		byteBuffer.reset();
		try(OutputStream output = Base64.getEncoder().wrap(byteBuffer)) {
			int read;
	        while((read = read(buffer)) >= 0) {
	            output.write(buffer, 0, read);
	        }
		}
		return byteBuffer.toString(StandardCharsets.UTF_8);
	}

	public Reader asReader() {
		return new InputStreamReader(this, charset);
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
}
