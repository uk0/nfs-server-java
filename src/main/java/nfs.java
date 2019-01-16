//
// Copyright 1997, 1998 Steven James Procter
// All rights under copyright reserved.
//  
// Everyone is granted permission to copy, modify and redistribute this code.
// Modification or redistribution for profit is expressely disallowed.
// The copyright notice and this notice must be preserved on all copies.
// 
// This software is distributed as-is.  No warranty of merchantability or
// fitness for any purpose is stated or implied.
// 
// It is my intention that anyone be able to use this software but not
// incorporate it or any part of it into any product or any software which
// is sold without first obtaining a licensing agreement from me the author.
// 
// 							Steven James Procter
// 							steven@void.org
// 							March 1997
//

class nfs implements NFSConsts, MountdConsts, RPCConsts { 
    rpcManager rpcmanager; // rpc packet dispatcher
    MountdHandler mountd;  // class to get and dispatch mountd requests
    NFSMTHandler nfs;	   // like mountdhandler for NFS requests
    Thread pmThread = null;// the portmapper thread
    
    // parameters
    String cacheFileName = "cache";
    String exportsFileName = "exports";
    boolean runPortmap = true; // run portmapper in another thread?

    // 
    // Prepare to run the NFS server.  The server is actually started with
    //   the method Run(), but this loads in all of the information and
    //   prepares everything to run.
    //
    public nfs(PathMapper pm, TimeMapper tm, FileSystemInfo fsi, String args[]){
	if (ProcessArguments(args) == false)
	    return;
	if (StartPortmapper() == false)
	    return;

	//
	// Create the RPC manager and register the request handlers for the
        //   services this RPC manager will provide, MOUNTD and NFS.
	//
	rpcmanager = new rpcManager((int) NFS_PORT);

	// Create the handle cache.  This is the list of file name to
	//   handle mappings used to make the NFS fhandle.
	Handle handles = new Handle("cache", pm);

	// create the handler for NFS calls
	nfs = new NFSMTHandler(handles, pm, fsi, tm);
	rpcmanager.RegisterHandler(nfs);
	PortmapRegister nfsreg = new PortmapRegister(nfs.Program(),
						     nfs.Version(),
						     UDPProto, NFS_PORT);
	
	// load in the exports file - if it doesn't exist all mount
	//   requests will be refused
	Exports exports = new Exports(exportsFileName, pm);
	System.out.println("reading exports file");
	if (exports.ReadExports() == false) {
	    System.err.println("!!! Warning: no exports file" +
			       " - no mounts will be allowed.");
	}
	System.out.println("  done");
	
	// create and register a mountd handler
	mountd = new MountdHandler(handles, exports, pm);
	rpcmanager.RegisterHandler(mountd);
	PortmapRegister mdreg = new PortmapRegister(mountd.Program(),
						    mountd.Version(),
						    UDPProto, NFS_PORT);
    };

    //
    // process the command line arguments.  Mainly this collects parameters
    //   from the user and sets flags (like runPortmap or exportsFileName).
    //
    boolean ProcessArguments(String args[]) {
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-p")) {
	        runPortmap = false;
	    }
	    else if (args[i].equals("-c")) {
		if (i + 1 >= args.length) {
		    System.err.println("Too few arguments for -c");
		    Usage();
		    return false;
		}
		i++;
		cacheFileName = args[i];
	    }
	    else if (args[i].equals("-e")) {
		if (i + 1 >= args.length) {
		    System.err.println("Too few arguments for -e");
		    Usage();
		    return false;
		}
		i++;
		cacheFileName = args[i];
	    }
	    else {
		Usage();
		return false;
	    }
	}
	return true;
    };
    // print out a usage message
    void Usage() {
	System.err.println("Usage: ntnfs [-p]" + 
			   " [-c cache-file-name]" +
			   " [-e exports-file-name]");
    }
    
    //
    // If the portmapper is to run in this process, create a thread for it and
    //   start it in that thread.
    //
    boolean StartPortmapper() {
	if (runPortmap) {
	    System.out.println("Starting portmapper.");
	    portmap pmap = new portmap();
	    pmThread = new Thread(pmap);
	    pmThread.start();

	    // give the portmap thread a little time to run
	    try {
		Thread.currentThread().sleep(3 * 1000);
	    } catch(InterruptedException e) {
		System.err.println("portmapper thread sleep interrupted");
		return false;
	    }
	}

	return true;
    };

    // Start getting and processing packets.  This procedure should not return.
    void Run() {
	// get packets and dispatch them forever
	rpcmanager.MainLoop();
    };
    
    public static void main(String args[]) {
	System.err.println("You cannot run NFS directly.  You need to specify");
	System.err.println("which platform you want to run on by starting the");
	System.err.println("NFS for that platform.  Currently supported are:");
	System.err.println("\tPlatform\tStart up command");
	System.err.println("\t--------\t----------------");
	System.err.println("\twindows 95\tjava win95nfs");
	System.err.println("\twindows NT\tjava ntnfs");
	System.err.println("\tunix\t\tjava unixnfs");
    };
};
