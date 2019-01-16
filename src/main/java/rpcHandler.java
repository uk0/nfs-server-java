abstract class rpcHandler {
    long program;
    long version;
    rpcHandler next;

    rpcHandler(long prog, long vnum) { 
	program = prog; 
	version = vnum; 
	next = null; 
    }
    
    public long Program() { return program; }
    public long Version() { return version; }
    
    public rpcHandler Next() { return next; }
    public void SetNext(rpcHandler to) { next = to; }
    
    public abstract void Run(UDPPacketPort socket, long xid, 
			     long procedure, XDRPacket packet);
};
