import java.io.*;

class portmap implements Runnable {
    public static void main(String args[]) {
	portmap pm = new portmap();
	pm.DoPortmap();
    }
    
    public void run() {
	DoPortmap();
    }
    
    void DoPortmap() {
	rpcManager manager = new rpcManager(111);

	// register the port mapper
	portMapHandler pm = new portMapHandler();
	manager.RegisterHandler(pm);
	
	// get packets and dispatch them forever
	manager.MainLoop();
    };
};
