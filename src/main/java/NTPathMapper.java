import java.util.*;

class NTPathMapper implements PathMapper {
    // make a single path out of two known good components
    public String MakePath(String head, String tail) {
	if (tail.compareTo(".") == 0)
	    return head;
	else if (tail.compareTo("..") == 0) {
	    // if the head is a volume name like c:\. then return it
	    if (head.regionMatches(1, ":\\.", 0, 3))
	        return head;
	    // else look for the last component of the path and take it off
	    int end = head.lastIndexOf('\\');
	    if (end < 0) {
		System.err.print("MakeString couldn't find final \\ in " 
				 + head + "\n");
		// fall through to the brute force case at the end
	    }
	    else {
		if (end == 2) // is the result like C:\ ? then add a dot.
		    return new String(head.substring(0, end) + "\\.");
		else
		    return new String(head.substring(0, end));
	    }
	}

	String result = new String(head + "\\" + tail);
	return Canonicalize(result);
    }

    // return the canonical form of a path.  There is a lot of aliasing
    //   possible with path names, ie \a\b = \a\.\.\b\c\.. - this procedure
    //   converts a path into the unique canonical representation
    public String Canonicalize(String input) {
	StringTokenizer str = new StringTokenizer(input, ":\\", false);
	int component = 0; // which component of path are we on - 1st is volume

	// copy all parts of this path into a vector for processing - remove
	//   all . items here
	Vector components = new Vector(30, 10);
	while (str.hasMoreTokens()) {
	    String next = str.nextToken();
	    if (next.equals("."))
	        continue;
	    components.addElement(next);
	}

	// remove all .. items that can be removed by looking for item\..
	//   pairs and removing them.  Note that c:\.. = c: 
	for (int i = 0; i < components.size() - 1; /*nothing*/) {
	    String next = (String) components.elementAt(i + 1);
	    if (next.equals("..")) {
		// get rid of the ..
		components.removeElementAt(i + 1);
		// and get rid of the component if it isn't the volume name
		if (i > 0) 
		    components.removeElementAt(i);
		// reconsider previous element for cases like \a\b\..\..
		if (i > 0)
		    i = i - 1;
	    }
	    else
	        i++;
	}
	// if there is nothing left, this is an empty path
	if (components.size() == 0)
	    return null;
	
	//
	// make a string out of the remaining components, of the form c:\a\b
	//
	StringBuffer result = new StringBuffer();
	if (components.size() == 1) 
	    // special case for win95, if it is just a volume make it like c:\.
	    result.append(components.elementAt(0) + ":\\.");
	else {
	    // stick on the volume name followed by a :
	    result.append(components.elementAt(0) + ":");
	    // append the components with separators
	    for (int i = 1; i < components.size(); i++) 
	        result.append("\\" + components.elementAt(i));
	}
	return result.toString();
    }

    // convert a unix path representation to a local path.  The / separators
    //   get turned into \ separators and the first component of the unix
    //   path becomes the volume name.  
    public String Convert(String unix) {
	StringTokenizer str = new StringTokenizer(unix, "/", false);
	StringBuffer result = new StringBuffer();

	int component = 0;
	while (str.hasMoreTokens()) {
	    String next = str.nextToken();
	    if (component == 0) 
		result.append(next + ":");
	    else
	        result.append("\\" + next);
	    component++;
	}
	// special case for paths to root for win 95
	if (component == 1)
	    result.append("\\.");
	
	return result.toString();
    }
    
    public static void main(String args[]) {
	NTPathMapper pm = new NTPathMapper();
	System.out.print("result is " + pm.MakePath(args[0], args[1]) + "\n");
    }
};
