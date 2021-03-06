/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Maximilian Froehling alias Nummer378/GermanCoding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package com.germancoding.packetapi.udp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UnreliableSocket {

	private DatagramSocket socket;
	private InetAddress remoteAddress;
	private int remotePort;
	private OutputStream out;
	private InputStream in;

	// Max packet size for a DatagramPacket (UnreliableInputStream & ...OutputStream)
	public static final int MAX_PACKET_SIZE = 8192;

	/**
	 * Creates a new UnreliableSocket with the given <code>socket</code> as the underlying socket. The socket can be in any state (connected, data flow already started...) as long as it isn't closed.<br>
	 * After calling this constructor, you should call the connect() method if the underlying socket isn't already connected<br>
	 * If necessary, the receiver buffer size of the underlying socket is changed to receive large packets. A <code>SocketException</code> is thrown if this fails.
	 * 
	 * @param socket
	 *            The underlying socket where data is send and received.
	 * @throws SocketException
	 *             If buffer resizing fails
	 */
	public UnreliableSocket(DatagramSocket socket) throws SocketException {
		this.socket = socket;
		if (socket.getReceiveBufferSize() < UnreliableSocket.MAX_PACKET_SIZE)
			socket.setReceiveBufferSize(UnreliableSocket.MAX_PACKET_SIZE); // Fix buffer size so that we can receive large packets
		if (socket.isConnected()) {
			connect(socket.getInetAddress(), socket.getPort());
		}
		setOut(new UnreliableOutputStream(this));
		setIn(new UnreliableInputStream(this));
	}

	/**
	 * "Connects" (Remember, UDP is a connection-less protocol) the socket to another socket. This method doesn't send any data to the given socket, it just remembers the given values. <br>
	 * You HAVE TO call this method if you want to send data (over the OutputStream provided by this socket).<br>
	 * Once connected, the InputStream will only accept data received from this remote address.<br>
	 * This method doesn't call the connect() methods of the underlying socket. If you call the connect() methods of the underlying socket manually, don't forget to call the connect() method of this class!<br>
	 * It's possible to call the connect() method multiple times, sender and receiver will immediatly update
	 * 
	 * @param address
	 *            Remote address to remember
	 * @param port
	 *            Remote port to remember
	 */
	public void connect(InetAddress address, int port) {
		this.setRemoteAddress(address);
		this.setRemotePort(port);
	}

	/**
	 * @return The underlying socket where data is actually send and received
	 */
	public DatagramSocket getSocket() {
		return socket;
	}

	/**
	 * Closes the underlying socket.
	 * 
	 * @throws IOException
	 *             When closing of a stream fails. Should never happen.
	 */
	public void close() throws IOException {
		if (!socket.isClosed()) {
			socket.close();
			getOutputStream().close();
			getInputStream().close();
			out = null;
			in = null;
		}
	}

	/**
	 * @return The remote address to which this socket is connected (Set by the connect() method).
	 */
	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * @see #connect(InetAddress, int)
	 */
	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/**
	 * @return The remote port to which this socket is connected (Set by the connect() method).
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * @see #connect(InetAddress, int)
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * @return An OutputStream provided by this instance. The OutputStream writes all data into UDP packets and sends them over the underlying socket. As usual in UDP, packets can be lost or received out of order. <br>
	 *         Note: Call the connect() method first before using this OutputStream. <b>Always call the flush() method, otherwise data is only buffered!</b>
	 */
	public OutputStream getOutputStream() {
		return out;
	}

	/**
	 * @return An InputStream provided by this instance. The InputStream reads data from UDP packets which are received from the underlying socket. As usual in UDP, packets can be lost or received out of order. <br>
	 *         Note: If connected, the InputStream will only accept data send from the connected remote address.
	 */
	public InputStream getInputStream() {
		return in;
	}

	// It's private since no one should override the I/O streams (they aren't final though - after closing the should be set to null)
	private void setOut(OutputStream out) {
		this.out = out;
	}

	private void setIn(InputStream in) {
		this.in = in;
	}

}
