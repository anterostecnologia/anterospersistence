/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.sql.lob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import br.com.anteros.core.utils.KMPSearchAlgorithm;

public class AnterosBlob implements Blob {

	public static final long MIN_POS = 1L;
	public static final long MAX_POS = 1L + (long) Integer.MAX_VALUE;
	private boolean closed;
	private byte[] bytes;

	public AnterosBlob(final byte[] bytes) throws SQLException {
		if (bytes == null)
			throw new SQLException("Null argument bytes");
		this.bytes = bytes;
	}

	protected AnterosBlob() {
		bytes = new byte[0];
	}

	public long length() throws SQLException {
		return getData().length;
	}

	public byte[] getBytes(long pos, final int length) throws SQLException {

		final byte[] data = getData();
		final int dataLength = data.length;

		if (pos < MIN_POS || pos > MIN_POS + dataLength) {
			throw new SQLException("Out of range argument pos: " + pos);
		}
		pos--;

		if (length < 0 || length > dataLength - pos) {
			throw new SQLException("Out of range argument length: " + length);
		}

		final byte[] result = new byte[length];
		System.arraycopy(data, (int) pos, result, 0, length);
		return result;
	}

	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(getData());
	}

	public long position(final byte[] pattern, final long start) throws SQLException {

		final byte[] data = getData();
		final int dataLength = data.length;

		if (start < MIN_POS) {
			throw new SQLException("Out of range argument start: " + start);
		} else if (start > dataLength || pattern == null) {
			return -1L;
		}
		final int startIndex = (int) start - 1;
		final int plen = pattern.length;

		if (plen == 0 || startIndex > dataLength - plen) {
			return -1L;
		}

		final int result = KMPSearchAlgorithm.search(data, pattern, KMPSearchAlgorithm.computeTable(pattern),
				startIndex);

		return (result == -1) ? -1 : result + 1;
	}

	public long position(final Blob pattern, long start) throws SQLException {

		final byte[] data = getData();
		final int dlen = data.length;

		if (start < MIN_POS) {
			throw new SQLException("Out of range argument start: " + start);
		} else if (start > dlen || pattern == null) {
			return -1L;
		}
		final int startIndex = (int) start - 1;
		final long plen = pattern.length();

		if (plen == 0 || startIndex > ((long) dlen) - plen) {
			return -1L;
		}

		final int iplen = (int) plen;
		byte[] bytePattern;

		if (pattern instanceof AnterosBlob) {
			bytePattern = ((AnterosBlob) pattern).data();
		} else {
			bytePattern = pattern.getBytes(1L, iplen);
		}

		final int result = KMPSearchAlgorithm.search(data, bytePattern, KMPSearchAlgorithm.computeTable(bytePattern),
				startIndex);

		return (result == -1) ? -1 : result + 1;
	}

	public int setBytes(long pos, byte[] bytes) throws SQLException {

		if (bytes == null) {
			throw new SQLException("Null argument bytes");
		}

		return setBytes(pos, bytes, 0, bytes.length);
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {

		if (bytes == null) {
			throw new SQLException("Null argument bytes");
		}

		if (offset < 0 || offset > bytes.length) {
			throw new SQLException("Out of range argument offset: " + offset);
		}

		if (len > bytes.length - offset) {
			throw new SQLException("Out of range argument len: " + len);
		}

		if (pos < MIN_POS || pos > 1L + (Integer.MAX_VALUE - len)) {
			throw new SQLException("Out of range argument pos: " + pos);
		}
		pos--;

		byte[] data = getData();
		final int dlen = data.length;

		if ((pos + len) > dlen) {
			byte[] temp = new byte[(int) pos + len];
			System.arraycopy(data, 0, temp, 0, dlen);
			data = temp;
			temp = null;
		}
		System.arraycopy(bytes, offset, data, (int) pos, len);
		checkClosed();
		setData(data);
		return len;
	}

	public OutputStream setBinaryStream(final long pos) throws SQLException {
		if (pos < MIN_POS || pos > MAX_POS) {
			throw new SQLException("Out of range argument pos: " + pos);
		}

		return new java.io.ByteArrayOutputStream() {

			public synchronized void close() throws java.io.IOException {

				try {
					AnterosBlob.this.setBytes(pos, toByteArray());
				} catch (SQLException se) {
					throw new IOException(se);
				} finally {
					super.close();
				}
			}
		};
	}

	public void truncate(final long len) throws SQLException {
		final byte[] data = getData();
		if (len < 0 || len > data.length) {
			throw new SQLException("Out of range argument len: " + len);
		}

		if (len == data.length) {
			return;
		}

		byte[] newData = new byte[(int) len];
		System.arraycopy(data, 0, newData, 0, (int) len);
		setData(newData);
	}

	public synchronized void free() throws SQLException {
		closed = true;
		bytes = null;
	}

	public InputStream getBinaryStream(long pos, long length) throws SQLException {

		final byte[] data = getData();
		final int dlen = data.length;

		if (pos < MIN_POS || pos > dlen) {
			throw new SQLException("Out of range argument pos: " + pos);
		}
		pos--;

		if (length < 0 || length > dlen - pos) {
			throw new SQLException("Out of range argument length: " + length);
		}

		if (pos == 0 && length == dlen) {
			return new ByteArrayInputStream(data);
		}

		final byte[] result = new byte[(int) length];
		System.arraycopy(data, (int) pos, result, 0, (int) length);
		return new ByteArrayInputStream(result);
	}

	protected byte[] data() throws SQLException {
		return getData();
	}

	private synchronized byte[] getData() throws SQLException {
		return bytes;
	}

	private synchronized void setData(byte[] data) throws SQLException {
		bytes = data;
	}

	protected synchronized void checkClosed() throws SQLException {
		if (closed) {
			throw new SQLException("Blob is closed.");
		}
	}
}