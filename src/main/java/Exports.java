import java.io.*;
import java.net.*;

class Exports {
    ExportsItem explist = null;
    String exportsFile = null;
    PathMapper pathmapper;

    Exports(String file, PathMapper pm) {
	explist = null;
	exportsFile = file;
	pathmapper = pm;
    }

    boolean Matches(InetAddress ia, String path) {
	// convert the address to a long
	byte [] addr = ia.getAddress();
	long ip = ToIPLong(addr);

	for (ExportsItem ei = explist; ei != null; ei = ei.next) {
	    if (path.compareTo(ei.path) == 0) {
		long mask = BitsToBitmask(ei.bits);
		System.out.print("mask=" + Long.toString(mask, 16)
				 + " source=" + Long.toString(ip, 16)
				 + " config=" + Long.toString(ei.ipaddr, 16)
				 + "\n");
		if ((ip & mask) == (ei.ipaddr & mask)) 
		    return true;
	    }
	}
	return false;
    }

    boolean ReadExports() {
	try {
	    return Parse(exportsFile);
	} catch (IOException e) {
	    return false;
	} 
    }
    
    boolean Parse(String fileName) 
      throws FileNotFoundException, IOException {
	FileInputStream in = new FileInputStream(fileName);
	StreamTokenizer tok = new StreamTokenizer(in);
	tok.resetSyntax();
	tok.wordChars(16, 127);
	tok.whitespaceChars(' ', ' ');
	tok.whitespaceChars('\t', '\t');
	tok.whitespaceChars('\n', '\n');
	tok.whitespaceChars('\r', '\r');
	tok.commentChar('#');

	// keep parsing lines of input until there are no more
	for (;;) {
	    ExportsItem ei = new ExportsItem();

	    // get the path, the first word of the line
	    tok.nextToken();
	    switch(tok.ttype) {
	    case StreamTokenizer.TT_EOF:
	        return true;
	    case StreamTokenizer.TT_WORD:
	        ei.path = pathmapper.Canonicalize(tok.sval);
	        break;
	    default:
		System.err.print("parse error in exports file line " +
				 tok.lineno() + " reading path\n");
		System.err.print("token=" + tok.ttype + "\n");
		return false;
	    }
	
	    tok.nextToken();
	    if (tok.ttype == StreamTokenizer.TT_WORD) {
		String ip = tok.sval;
		if (ParseIP(ip, tok.lineno(), ei) == false) {
		    System.err.print("error parsing ip address line "
				     + tok.lineno() + "\n");
		    return false;
		}
	    }
	    else {
		System.err.print("parse error on line " + tok.lineno() 
				 + " trying to read ip addr after path\n");
		return false;
	    }

	    // add this exports item to the list
	    if (explist == null) {
	        explist = ei;
		explist.next = null;
	    }
	    else {
		ei.next = explist;
		explist = ei;
	    }
	    ei.Print();
	}
    }
    

    boolean ParseIP(String spec, int lineno, ExportsItem expitem) {
	int position = 0; // offset in string, points to current character
	long ipaddr = 0;  // ip address under construction

	// addresses are specified as [0-9]+{\.[0-9]+}*, a number followed
	//   by up to 3 more numbers preceded by dots.  Numbers must be 0..255
	for (int octet = 0; /*nothing*/; octet++) {
	    int nextPosition = position;
	    while (nextPosition < spec.length() &&
		   Character.isDigit(spec.charAt(nextPosition)))
	        nextPosition++;
	    if (nextPosition == position)
	        break;
	    
	    // add this number to the ip address being constructed
	    String num = spec.substring(position, nextPosition);
	    long l = Long.parseLong(num);
	    if (l < 0 || l > 255) {
		System.err.print("ip address octet " + l + " out of range"
				 + " on line " + lineno + "\n");
		return false;
	    }
	    if (octet > 3) {
		System.err.print("too many octets in ip address line " 
				 + lineno + "\n");
		return false;
	    }
	    ipaddr += l << (8 * (3 - octet));
	    position = nextPosition;
	    
	    // if the next char is a . then expect some more ip address
	    if (position < spec.length() && spec.charAt(position) == '.') 
		position++;
	    else
	        break;
	}

	long bits = 32;
	// if there is more then if the next char is a /, there is a bits
	//   specifier in this word.  Otherwise if it isn't a space, error.
	if (position < spec.length()) {
	    if (spec.charAt(position) == '/') {
		position++; // move past the /
		int nextPosition = position;
		while (nextPosition < spec.length() &&
		       Character.isDigit(spec.charAt(nextPosition)))
		    nextPosition++;
		// if there is a number after position, read it into bits
		if (nextPosition > position) {
		    String bitSpec = spec.substring(position, nextPosition);
		    bits = Long.parseLong(bitSpec);
		}
	    }
	    else if (Character.isSpace(spec.charAt(position)) == false) {
		System.err.print("illegal char (" + spec.charAt(position)
				 + ") offset " + position 
				 + " in the ip address line " + lineno + "\n");
		return false;
	    }
	}
	
	expitem.ipaddr = ipaddr;
	expitem.bits = bits;
	return true;
    }

    // convert a number of significant bits to a bitmask that has that many
    //   bits on starting at bit 31.
    static long BitsToBitmask(long bits) {
	long result = (1 << (int) bits) - 1;
	result <<= (int) (32 - bits);
	return result;
    }
        
    // convert an array of bytes as returned from InetAddress.getAddress()
    //   into a long.
    long ToIPLong(byte [] addr) {
	long result = 0;
	for (int i = 0; i < 4; i++) {
	    long item = addr[i];
	    if (item < 0) // convert to unsigned
	        item += 256;
	    result += item << (8 * (3 - i)); // shift into position
	}
	return result;
    }

    void Print() {
	for (ExportsItem ei = explist; ei != null; ei = ei.next)
	    ei.Print();
    }

    public static void main(String args[]) {
	NTPathMapper pm = new NTPathMapper();
	Exports e = new Exports("exports", pm);
	e.ReadExports();
    }
}

class ExportsItem {
    String path;
    long   ipaddr;
    long   bits;
    ExportsItem next;


    ExportsItem() {
	path = null;
	next = null;
    }
    void Print() { 
	System.out.print("ExportsItem(path=" + path + ", ipaddr=" 
			 + Long.toString(ipaddr, 16)
			 + ", bits=" + bits + ")\n");
    }
};
