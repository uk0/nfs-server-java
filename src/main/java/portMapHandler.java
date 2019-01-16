import java.io.*;
import java.util.Hashtable;
import java.net.*;

class portmapMapping {
    long prog;
    long vers;
    long prot;
    long port;
    portmapMapping next;

    portmapMapping(long pg, long vr, long pr) { 
	prog = pg;
	vers = vr;
	prot = pr;
	next = null; 
    }
    
    // some accessor functions
    long Program() { return prog; }
    long Version() { return vers; }
    long Protocol() { return prot; }
    long Port() { return port; }
    void SetPort(long to) { port = to; }
    portmapMapping Next() { return next; }
    void SetNext(portmapMapping to) { next = to; }

    // required functions to put this into a hashtable
    public int hashCode() {
	return (int) (prog * vers + prog * prot + vers * prot);
    };

    public boolean equals(Object o) {
	if (o instanceof portmapMapping) {
	    portmapMapping mi = (portmapMapping) o;
	    if (mi.prog == prog && mi.vers == vers && mi.prot == prot)
		return true;
	    return false;
	}
	return false;
    };
};

// Implement the portmapper procedures.
class portMapHandler extends rpcHandler implements RPCConsts, PortMapConst {
    Hashtable mappings;

    portMapHandler() {
	super(PM_PROG, PM_VERS);
	
	mappings = new Hashtable();

	Register(PM_PROG, PM_VERS, UDPProto, 111);
    };

    public void Run(UDPPacketPort port, long xid,
		    long procedure, XDRPacket packet) {
	System.out.print("portmap run method called\n");
	switch((int) procedure) {
	case (int) PMAP_NULL:
	    System.out.print("PMAP_NULL called\n");
	    PMNull(port, xid, procedure, packet);
	    break;
	case (int) PMAP_SET:
	    System.out.print("PMAP_SET called\n");
	    PMSet(port, xid, procedure, packet);
	    break;
	case (int) PMAP_UNSET:
	    System.out.print("PMAP_UNSET called\n");
	    break;
	case (int) PMAP_GETPORT:
	    System.out.print("PMAP_GETPORT called\n");
	    PMGetPort(port, xid, procedure, packet);
	    break;
	case (int) PMAP_DUMP:
	    System.out.print("PMAP_DUMP called\n");
	    break;
	case (int) PMAP_CALLIT:
	    System.out.print("PMAP_CALLIT called\n");
	    break;
	default:
	    System.out.print("default called\n");
	    break;
	}
    };

    void PMNull(UDPPacketPort port, long xid, long procedure,
		   XDRPacket packet) {
	// Put together an XDR reply packet
	XDRPacket result = new XDRPacket(128);
	result.Reset();
	
	result.AddLong(xid);
	result.AddLong(RPCReply);
	result.AddLong(RPCMsgAccepted);

	// Put on a NULL authentication
	result.AddNullAuthentication();

	result.AddLong(RPCSuccess);

	// send the reply back
	System.out.print("Sending reply back to address " +
			 packet.Source().getHostAddress() + " port " +
			 packet.Port() + "\n");
	port.SendPacket(packet.Source(), packet.Port(), result);
    };

    void PMGetPort(UDPPacketPort port, long xid, long procedure,
		   XDRPacket packet) {
        long begin = System.currentTimeMillis(); // XXX GROT

	// skip past the authentication records
	packet.ReadAuthentication();
	packet.ReadAuthentication();

	// Collect the arguments to the procedure
	long prog = packet.GetLong();
	long vers = packet.GetLong();
	long prot = packet.GetLong();

	// Put together an XDR reply packet
	XDRPacket result = new XDRPacket(128);
	result.AddLong(xid);
	result.AddLong(RPCReply);
	result.AddLong(RPCMsgAccepted);
	result.AddNullAuthentication();

	System.out.print("Looking for prog " + prog + " vers "
			 + vers + " prot " + prot + "\n");

	Long pl = new Long(prog);
	portmapMapping chain = (portmapMapping) mappings.get(pl);
	if (chain == null) {
	    // if there is no chain for this program number, it just
	    //   isn't registered with me
	    System.out.print("No handlers for that program registered\n");
	    result.AddLong(PM_PROG_UNAVAIL);
	}
	else {
	    // if there is a chain, look for the requested version
	    long versmin = chain.Version();
	    long versmax = chain.Version();
	    while (chain != null) {
		if (chain.Version() == vers) {
		    System.out.print("Found program/version on port " + 
				     chain.Port() + "\n");
		    result.AddLong(RPCSuccess);
		    result.AddLong(chain.Port());
		    break;
		}
		else if (chain.Version() < versmin)
		    versmin = chain.Version();
		else if (chain.Version() > versmax)
		    versmax = chain.Version();
		
		chain = chain.Next();
	    }
	    if (chain == null) { // didn't find the correct version
		System.out.print("Version mismatch: only have " + versmin
				 + " to " + versmax + "\n");
		result.AddLong(RPCProgMismatch);
		result.AddLong(versmin);
		result.AddLong(versmax);
	    }
	}

	// send the reply back
	System.out.print("Sending reply back to address " +
			 packet.Source().getHostAddress() + " port " +
			 packet.Port() + "\n");
	port.SendPacket(packet.Source(), packet.Port(), result);

        long end = System.currentTimeMillis(); // XXX GROT
        System.err.println("PMGetPort took " + (end - begin) + "ms"); //XXX GROT
    };
    
    void PMSet(UDPPacketPort udp, long xid, long procedure,
		   XDRPacket packet) {
	// skip past the authentication records
	packet.ReadAuthentication();
	packet.ReadAuthentication();

	// Collect the arguments to the procedure
	long prog = packet.GetLong();
	long vers = packet.GetLong();
	long prot = packet.GetLong();
	long port = packet.GetLong();
	portmapMapping toadd = new portmapMapping(prog, vers, prot);
	toadd.SetPort(port);

	XDRPacket result = new XDRPacket(128);
	result.AddReplyHeader(xid);
	
	// look for the chain of versions for this program
	Long pl = new Long(prog);
	portmapMapping chain = (portmapMapping) mappings.get(pl);
	if (chain == null) {
	    mappings.put(pl, toadd);
	    result.AddLong(PM_TRUE);
	}
	else {
	    // See if this version is already registered in the chain
	    while (chain != null) {
		if (chain.Version() == vers && chain.Protocol() == prot) {
		    result.AddLong(PM_FALSE);
		    break;
		}
		else if (chain.Next() == null) {
		    chain.SetNext(toadd);
		    result.AddLong(PM_TRUE);
		    break;
		}
		chain = chain.Next();
	    }
	}

	System.out.print("Registering prog " + prog + " vers "
			 + vers + " prot " + prot + 
			 " port " + port + "\n");

	udp.SendPacket(packet.Source(), packet.Port(), result);
    };
    
    void Register(long prog, long vers, long prot, long port) {
	PortmapRegister pr = new PortmapRegister(prog, vers, prot, port);
    }
};

class PortmapRegister implements RPCConsts, PortMapConst {

    //
    // Send a registration request for this program to the portmapper.
    //
    PortmapRegister(long procid, long vers, long proto, long port) {
	XDRPacket packet = new XDRPacket(128);
	
	packet.AddLong(0);      // xid
	packet.AddLong(RPCCall);// type
	
	packet.AddLong(2);      // rpc version
	packet.AddLong(PM_PROG); // portmapper program number
	packet.AddLong(2);      // portmapper version
	packet.AddLong(PMAP_SET); // PM_SET
	
	packet.AddNullAuthentication();
	packet.AddNullAuthentication();
	
	packet.AddLong(procid); // program to register
	packet.AddLong(vers);   // version to register
	packet.AddLong(proto);  // protocol to register
	packet.AddLong(port);   // port portmapper listens on
	
	UDPPacketPort udp = new UDPPacketPort(-1);
	udp.InitializePort();
	
	try {
	    InetAddress ia;
	    ia = InetAddress.getLocalHost();
	    udp.SendPacket(ia, (int) PMAP_PORT, packet);
	} catch(UnknownHostException e) {
	    System.err.print("Couldn't get address for localhost\n");
	}
	
    };
};

