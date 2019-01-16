interface RPCConsts {
    // RPC call options
    public static int RPCCall = 0;
    public static int RPCReply = 1;

    // RPC results
    public static int RPCMsgAccepted = 0;
    public static int RPCMsgDenied = 0;
    
    // Status of procedure call
    public static int RPCSuccess = 0;
    public static int RPCProgUnavail = 1;
    public static int RPCProgMismatch = 2;
    public static int RPCProcUnavail = 3;
    public static int RPCGarbageArgs = 4;
    
    public static long UDPProto = 17;
    public static long TCPProto = 6;
};
    
