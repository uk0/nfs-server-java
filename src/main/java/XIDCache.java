import java.util.*;

class XIDCache implements Runnable {
    Hashtable items;
    long gcPeriodMS = 10 * 1000; // number of ms between runs of the gc
    long packetLifetimeMS = 30 * 1000; // time in ms that a packet stays in cache

    XIDCache() {
	items = new Hashtable();
        // start the garbage collector 
        Thread thd = new Thread(this);
        thd.start();
    }
    
    // start a new xid in the queue
    boolean Start(long xid) {
	if (Find(xid) != null) {
	    System.err.print("Start xid=" + xid + " already queued\n");
	    return false;
	}
	
	// make a new xid queue item that isn't complete
	XIDCacheItem qi = new XIDCacheItem();
	qi.xid = xid;
	qi.timeInQueueMS = System.currentTimeMillis();
	qi.inprogress = true;
	qi.packet = null;
	
	// and put it in the queue
	Add(qi);
	return true;
    }
    
    // set the reply packet for this xid and turn off inprogress
    synchronized boolean SetPacket(long xid, XDRPacket packet) {
	XIDCacheItem qi = Find(xid);
	if (qi == null) {
	    System.err.print("SetPacket xid=" + xid + " not found\n");
	    return false;
	}
	qi.inprogress = false;
	qi.packet = packet;
	return true;
    }

    synchronized void Add(XIDCacheItem qi) {
        // put this item into the hashtable
	items.put(new Long(qi.xid), qi);
    }
    
    synchronized XIDCacheItem Find(long xid) {
        return (XIDCacheItem) items.get(new Long(xid));
    }

    //
    // The XIDCache garbage collector runs in its own thread, sleeping for
    //   a while then removing items that are more than a certain number of
    //   seconds old.
    //

    // run forever sleeping for a 10 seconds then removing old items from
    //   the table
    synchronized void Clean() {
        for (;;) {
            try {
                wait(gcPeriodMS);
            }
            catch(InterruptedException ie) {
                System.err.println("XIDCache:Clean: wait interrupted");
            }

            long currentTime = System.currentTimeMillis();
            System.out.println("XIDCache gc running: " + items.size()
                               + " items in the XID cache");

            Enumeration keys = items.keys();
            Enumeration elements = items.elements();
            while (keys.hasMoreElements()) {
                Long key = (Long) keys.nextElement();
                XIDCacheItem item = (XIDCacheItem) elements.nextElement();

                // see if this item needs to be removed from the cache
                long timeInQueue = currentTime - item.timeInQueueMS;
                if (timeInQueue > packetLifetimeMS)
                    items.remove(key);
            }

            long endTime = System.currentTimeMillis();
            System.err.println("XIDCache: gc took " + (endTime - currentTime)
                               + "ms to run");
        }
    }

    public void run() {
        Clean();
        System.err.println("Warning: XIDCache garbage collector exited.");
        System.err.println("  Don't expect the system to keep running very long");
    }
    
    public static void main(String args[]) {
	XIDCache q = new XIDCache();

	for (int i = 0; i < 1000; i++ ) {
	    q.Start(i);
	}
	q.Clean();
	try {
	    Thread.currentThread().sleep(1000);
	} catch (InterruptedException e) {
	    System.err.print("XIDCache:main: thread sleep was interrupted\n");
	}
	q.Clean();

	XIDCacheItem qi = q.Find(36);
	if (qi == null) 
            System.out.print("no item 36 found\n");
	else
            qi.Print();
    }

    class XIDCacheItem {
        long xid;
        long timeInQueueMS; // time it has been in the queue
        boolean inprogress; // is this packet in progress or done
        XDRPacket packet;
    
        void Print() {
            System.out.print("XIDCacheItem(xid=" + xid + ", timeInQueue="
                             + timeInQueueMS + ", inprogress=" + inprogress
                             + ")\n");
        }
    }
}
