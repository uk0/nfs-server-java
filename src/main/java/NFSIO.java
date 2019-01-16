import java.io.*;

class NFSIO implements NFSConsts, RPCConsts {
    Handle handles;
    TimeMapper tm;

    NFSIO(Handle h, TimeMapper t) {
	handles = h;
	tm = t;
    }

    XDRPacket Write(long xid, XDRPacket packet, XDRPacketCache packetCache)
        throws NFSException {
	String fileName = null;

	try {
	    // read info out of packet
	    fhandle fh = new fhandle(packet);
	    long beginoffset = packet.GetLong();
	    long offset = packet.GetLong();
	    long totalcount = packet.GetLong();
	    // next comes the data which is a long of size and the bytes.
	    long datalen = packet.GetLong();
	    long packetOffset = packet.CurrentPosition();
	    packet.Advance(datalen);

	    // carry out the write operation
	    fileName = handles.Lookup(fh.Handle());
	    if (fileName == null)
	        throw new NFSException(xid, NFSERR_STALE);
	    // XXX comment out print lines to improve performance
	    // System.out.print("Write(" + fileName + ", " + offset + ", " + 
	    //		     datalen + ")\n");

	    RandomAccessFile fd = new RandomAccessFile(fileName, "rw");
	    fd.seek(offset);
	    fd.write(packet.Data(), (int) packetOffset, (int) datalen);
	    fd.close();

	    // load in new file attributes
	    fattr fa = new fattr(handles, tm);
	    fa.Load(fileName);

	    // create the reply packet
	    XDRPacket writereply = packetCache.find(128);
	    writereply.Reset();
	    writereply.AddReplyHeader(xid);
	    writereply.AddLong(NFS_OK);
	    fa.Emit(writereply);
	    return writereply;

	} catch (IOException e) {
	    System.err.println("Exception writing " + fileName + " - " + e);
	    throw new NFSException(xid, NFSERR_IO);
	} catch (SecurityException e) {
	    throw new NFSException(xid, NFSERR_PERM);
	}
    };

    XDRPacket Read(long xid, XDRPacket packet, XDRPacketCache packetCache)
        throws NFSException {
	try {
	    // collect data out of the packet
	    fhandle fh = new fhandle(packet);
	    long offset = packet.GetLong();
	    long count = packet.GetLong();
	    long totalCount = packet.GetLong(); // not used
	    
	    // do the operation
	    String fileName = handles.Lookup(fh.Handle());
	    if (fileName == null)
	        throw new NFSException(xid, NFSERR_STALE);
	    // XXX Comment out read lines to improve performance
	    // System.out.print("Read(" + fileName + ", " + count + ", " + 
	    //		     offset + ")\n");
	    
	    if (count <= 0) {
		System.err.println("\tRead: invalid value for count " + count);
		throw new NFSException(xid, NFSERR_IO);
	    }

	    FileInputStream fd = new FileInputStream(fileName);
	    fd.skip(offset);
	    byte [] readbuf = new byte[(int) count];
	    int numberRead = fd.read(readbuf);
	    fd.close();
	    // XXX comment out prints to improve performance
	    // System.err.println("Read: got " + numberRead + " bytes");
	    
	    // Make sure something was read in */
	    if (numberRead < 0) {
		System.err.println("\tRead error: number read is " 
				   + numberRead);
		numberRead = 0;
	    }
	    
	    // load in file attributes.
	    fattr fa = new fattr(handles, tm);
	    fa.Load(fileName);

	    // put together the reply packet, this will copy the read
	    //   data which is a little inefficient
	    XDRPacket reply = packetCache.find(128 + numberRead);
	    reply.AddReplyHeader(xid);
	    reply.AddLong(NFS_OK);
	    fa.Emit(reply);
	    reply.AddData(numberRead, readbuf);

	    return reply;
	    
	} catch(FileNotFoundException e) {
	    throw new NFSException(xid, NFSERR_NOENT);
	} catch(IOException e) {
	    throw new NFSException(xid, NFSERR_IO);
	}
    }

    XDRPacket getXDRPacket(int desiredSize) {
        return new XDRPacket(desiredSize);
    }
};
