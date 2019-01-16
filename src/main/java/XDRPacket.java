import java.net.*;
import java.io.*;

class XDRPacket implements RPCConsts {
    // data contents of the packet
    byte data[];
    int  len;
    int  position;

    // store information about where the packet came from
    InetAddress source;
    int port;
    
    // control debugging output
    boolean debug = false;
    
    XDRPacket(DatagramPacket input) {
	data = input.getData();
	len = input.getLength();
	position = 0;
	source = input.getAddress();
	port = input.getPort();
    };
    
    XDRPacket(int size) {
	len = size; 
	data = new byte[len];
	position = 0;
    }

    byte [] Data() { return data; }
    int  Length() { return position; }
    int  CurrentPosition() { return Length(); }
    InetAddress Source() { return source; }
    int Port() { return port; }

    public void Reset() { position = 0; };

    // unpack the next long from an XDR packet
    public long GetLong() {
	long result = 0;
	for (int i = 0; i < 4; i++) {
	    long next = (long) data[position + i];
	    if (next < 0)  {
		// make sure it is positive
		next += 256;
	    }
	    result *= 256;
	    result += next;
	}

	position += 4;
	return result;
    };

    public String GetString() {
     	long len = GetLong();

	char [] buf = new char[(int) len];
	for (int i = 0; i < len; i++) 
	    buf[i] = (char) data[i + position];
	String result = new String(buf);
	
	Advance(len);
	return result;
    };

    public void AddLong(long l) {
	for (int i = 0; i < 4; i++) {
	    long rem = l % 256;
	    l = l / 256;

	    if (rem > 128)
		rem -= 256;
	    data[position + (3 - i)] = (byte) rem;
	}
	position += 4;
    };
    
    public long AddString(String s) {
	long length = (long) s.length();

	AddLong(length);
	for (long l = 0; l < length; l++)
	    data[(int) l + position] = (byte) s.charAt((int) l);
	Advance(length);
	
	return 4 + 4 * ((length + 3) / 4);
    }

    public long AddData(int len, byte [] toadd) {
	AddLong(len);
	System.arraycopy(toadd, 0, data, position, len);

	Advance(len);
	return 0;
    };
    public long AddData(byte [] toadd) {
	return AddData(toadd.length, toadd);
    };

    // copy the unprocessed data object starting at the current position in
    //   the buffer into the byte array.  Assume that the array is big enough.
    public long GetData(byte [] buffer) {
	long plen = GetLong(); // how much data is in the packet
	if (plen + position >= data.length) {
	    System.err.print("GetData: packet is too small\n");
	    return -1;
	}

	// try to copy the data into the buffer
	System.arraycopy(data, position, buffer, 0, (int) plen);

	Advance(plen);
	return plen;
    };
    
    // Add the standard procedure you requested was called reply header
    public void AddReplyHeader(long xid) {
	AddLong(xid);
	AddLong(RPCReply);
	AddLong(RPCMsgAccepted);
	AddNullAuthentication();
	AddLong(RPCSuccess);
    }

    public void AddNullAuthentication() {
	AddLong(0); /* the type */
	AddLong(0); /* the length */
    };
    
    public void ReadAuthentication() {
	long type = GetLong();
	long length = GetLong();
	Advance(length);
    };
    
    public void Advance(long length) {
	long words = (length + 3) / 4;
	long delta = 4 * words;
	position += (int) delta;
    }
}

