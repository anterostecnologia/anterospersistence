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
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

import br.com.anteros.core.utils.KMPSearchAlgorithm;

public class AnterosClob implements Clob {

	private static final long MIN_POS = 1L;
	private static final long MAX_POS = 1L + (long) Integer.MAX_VALUE;
	private boolean closed;
	private String bytes;

	public AnterosClob(final String data) throws SQLException {
		if (data == null) {
			throw new SQLException("Null argument bytes");
		}
		bytes = data;
	}

	protected AnterosClob() {
		bytes = "";
	}

	public long length() throws SQLException {
		return getData().length();
	}

	public String getSubString(long pos, final int length) throws SQLException {

		final String data = getData();
		final int dlen = data.length();

		if (pos < MIN_POS || pos > dlen) {
			throw new SQLException("Out of range argument pos: " + pos);
		}
		pos--;

		if (length < 0 || length > dlen - pos) {
			throw new SQLException("Out of range argument length: " + length);
		}

		return (pos == 0 && length == dlen) ? data : data.substring((int) pos, (int) pos + length);
	}

	public java.io.Reader getCharacterStream() throws SQLException {
		return new StringReader(getData());
	}

	public java.io.InputStream getAsciiStream() throws SQLException {
		try {
			return new ByteArrayInputStream(getData().getBytes("US-ASCII"));
		} catch (IOException e) {
			return null;
		}
	}

	public long position(final String searchstr, long start) throws SQLException {

		final String data = getData();

		if (start < MIN_POS) {
			throw new SQLException("Out of range argument start: " + start);
		}

		if (searchstr == null || start > MAX_POS) {
			return -1;
		}

		final int position = KMPSearchAlgorithm.search(data, searchstr, null, (int) start);

		return (position == -1) ? -1 : position + 1;
	}

	public long position(final Clob searchstr, long start) throws SQLException {

		final String data = getData();

		if (start < MIN_POS) {
			throw new SQLException("Out of range argument start: " + start);
		}

		if (searchstr == null) {
			return -1;
		}

		final long dlen = data.length();
		final long sslen = searchstr.length();

		start--;

		if (start > dlen - sslen) {
			return -1;
		}

		String pattern;

		if (searchstr instanceof AnterosClob) {
			pattern = ((AnterosClob) searchstr).data();
		} else {
			pattern = searchstr.getSubString(1L, (int) sslen);
		}

		final int position = KMPSearchAlgorithm.search(data, pattern, null, (int) start);

		return (position == -1) ? -1 : position + 1;
	}

	public int setString(long pos, String str) throws SQLException {
		if (str == null) {
			throw new SQLException("Null argument bytes");
		}
		return setString(pos, str, 0, str.length());
	}

	public int setString(long pos, String str, int offset, int len) throws SQLException {
		String data = getData();
		if (str == null) {
			throw new SQLException("Null argument bytes");
		}

		final int strlen = str.length();

		if (offset < 0 || offset > strlen) {
			throw new SQLException("Out of range argument offset: " + offset);
		}

		if (len > strlen - offset) {
			throw new SQLException("Out of range argument len: " + len);
		}

		if (pos < MIN_POS || pos > 1L + (Integer.MAX_VALUE - len)) {
			throw new SQLException("Out of range argument pos: " + pos);
		}

		final int dlen = data.length();
		final int ipos = (int) (pos - 1);
		StringBuilder sb;

		if (ipos > dlen - len) {
			sb = new StringBuilder(ipos + len);
			sb.append(data.substring(0, ipos));
			data = null;
			sb.append(str.substring(offset, offset + len));
			str = null;
		} else {
			sb = new StringBuilder(data);
			data = null;
			for (int i = ipos, j = 0; j < len; i++, j++) {
				sb.setCharAt(i, str.charAt(offset + j));
			}
			str = null;
		}
		setData(sb.toString());

		return len;
	}

	public java.io.OutputStream setAsciiStream(final long pos) throws SQLException {
		checkClosed();
		if (pos < MIN_POS || pos > MAX_POS) {
			throw new SQLException("Out of range argument pos: " + pos);
		}

		return new java.io.ByteArrayOutputStream() {

			public synchronized void close() throws java.io.IOException {

				try {
					AnterosClob.this.setString(pos, new String(toByteArray(), "US-ASCII"));
				} catch (SQLException se) {
					throw new IOException(se);
				} finally {
					super.close();
				}
			}
		};
	}

	public java.io.Writer setCharacterStream(final long pos) throws SQLException {
		checkClosed();

		if (pos < MIN_POS || pos > MAX_POS) {
			throw new SQLException("Out of range argument pos: " + pos);
		}

		return new java.io.StringWriter() {
			public synchronized void close() throws java.io.IOException {

				try {
					AnterosClob.this.setString(pos, toString());
				} catch (SQLException se) {
					throw new IOException(se);
				}
			}
		};
	}

	public void truncate(final long len) throws SQLException {

		final String data = getData();
		final long dlen = data.length();

		if (len == dlen) {
		} else if (len < 0 || len > dlen) {
			throw new SQLException("Out of range argument len: " + len);
		} else {
			setData(data.substring(0, (int) len));
		}
	}

	public synchronized void free() throws SQLException {
		closed = true;
		bytes = null;
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {

		if (length > Integer.MAX_VALUE) {
			throw new SQLException("Out of range argument length: " + length);
		}

		return new StringReader(getSubString(pos, (int) length));
	}

	protected synchronized void checkClosed() throws SQLException {
		if (closed) {
			throw new SQLException("Clob is closed.");
		}
	}

	protected String data() throws SQLException {
		return getData();
	}

	private synchronized String getData() throws SQLException {
		checkClosed();
		return bytes;
	}

	private synchronized void setData(String data) throws SQLException {
		checkClosed();
		bytes = data;
	}
}