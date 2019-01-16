class NFSOperation implements Runnable, NFSConsts, RPCConsts {
    MTList input;
    NFSHandler handler;

    NFSOperation(MTList inputq, NFSHandler nfsh) {
	input = inputq;
	handler = nfsh;
    }
    
    public void run() {
	for (;;) {
	    // System.out.print("thread waiting on nfs item queue\n");
	    NFSItem next = (NFSItem) input.Get();
	    //System.out.println("NFSOperation got item from queue xid=" + 
	    //  next.xid);
	    handler.Run(next.port, next.xid,
			next.procedure, next.packet);
	}
    }
};
