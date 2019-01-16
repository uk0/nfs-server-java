import java.util.*;

class UnixPathMapper implements PathMapper {
    public String MakePath(String directory, String component) {
	return Canonicalize(directory + "/" + component);
    }

    // Put path into canonical form, eg. remove any aliasing information.
    //   This means removing extra path separaters.
    public String Canonicalize(String path) {
        if (path.indexOf("//") < 0)
	    return path;

	StringBuffer sb = new StringBuffer(path);
	for (;;) {
	    String current = sb.toString();
	    int next = current.indexOf("//");
	    if (next < 0)
	        break;
	    sb.deleteCharAt(next);
	}

	return sb.toString();
    }

    public String Convert(String unixPath) {
    	return unixPath;
    }
}
