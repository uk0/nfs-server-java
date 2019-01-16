import java.net.*;

//
// Get the next packet from the network asynchronously, so it will be ready
//   for the main thread as soon as it is needed.
//

class PacketCollector implements Runnable {
    MTList packets;
    UDPPacketPort socket;
    DatagramPacket packet;
    
    PacketCollector(UDPPacketPort udpport, MTList tolist) { 
	socket = udpport;
	packets = tolist;
    }
    
    public synchronized void run() {
	byte [] buf = new byte[10240]; // bigger than max nfs packet
	for (;;) {
	    // Get the next packet from the network - only let one thread
	    //   at a time read from the socket.
	    packet = socket.GetPacket(buf);
	    // if the packet is something, add this to the list and wait
	    //   for the rpcmanager to process with it and say Done().
	    if (packet != null) {
	        packets.Add(this);
		try {
		    wait(); // wait for the packet handler to use this packet
		} catch (InterruptedException e) {
		    System.err.print("packet collector: interrupted wait\n");
		}
	    }
	}
    }
    public synchronized void Done() {
	// let the rpc manager notify this thread that the packet is done and
	//   can be reused.
	notify();
    }
    
    public PacketCollector Get() {
	Object o = packets.Get();
	if (o instanceof PacketCollector)
	    return (PacketCollector) o;
	
	System.err.print("PacketCollector Get: packet queue contains"
			 + " non-packet\n");
	return null;
    }
    
    // accessor method
    public DatagramPacket Packet() {
	return packet;
    }
};

