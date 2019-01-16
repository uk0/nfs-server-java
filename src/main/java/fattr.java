import java.io.*;
import java.util.Date;

// The file attributes that NFS needs.
//
// From:
// Sun Microsystems, Inc.                                         [Page 15]
// RFC 1094                NFS: Network File System              March 1989
//
class fattr implements NFSConsts, UnixPermissions {
    long         type;
    long	 mode;
    long	 nlink;
    long	 uid;
    long	 gid;
    long	 size;
    long	 blocksize;
    long	 rdev;
    long	 blocks;
    long	 fsid;
    long	 fileid;
    timeval      atime;
    timeval      mtime;
    timeval      ctime;
    
    static boolean debug = false;
    static TimeMapper tm;
    Handle fileHandles;
    
    fattr(Handle h, TimeMapper t) { fileHandles = h; tm = t; }
    fattr(boolean controlDebug) { debug = controlDebug; }

    boolean Read(XDRPacket from) {
	type = from.GetLong();
	mode = from.GetLong();
	nlink = from.GetLong();
	uid = from.GetLong();
	gid = from.GetLong();
	size = from.GetLong();
	blocksize = from.GetLong();
	rdev = from.GetLong();
	blocks = from.GetLong();
	fsid = from.GetLong();
	fileid = from.GetLong();

	if (atime.Read(from) == false)
	    return false;
	if (mtime.Read(from) == false)
	    return false;
	if (ctime.Read(from) == false)
	    return false;
	
	return true;
    };

    boolean Emit(XDRPacket to) {
	to.AddLong(type);
	to.AddLong(mode);
	to.AddLong(nlink);
	to.AddLong(uid);
	to.AddLong(gid);
	to.AddLong(size);
	to.AddLong(blocksize);
	to.AddLong(rdev);
	to.AddLong(blocks);
	to.AddLong(fsid);
	to.AddLong(fileid);
	
	if (atime.Emit(to) == false)
	    return false;
	if (mtime.Emit(to) == false)
	    return false;
	if (ctime.Emit(to) == false)
	    return false;
	
	return true;
    };

    long Load(String file) throws FileNotFoundException {
	File fd = new File(file);
	if (fd.exists() != true) {
	    if (debug) {
		System.out.print("fattr: file " + file + " doesn't exist.\n");
	        throw new FileNotFoundException();
	    }
	}

	mode = 0;
	if (fd.isFile()) {
	    if (debug)
		System.out.print("fattr: " + file + " is a file\n");
	    type = FT_NFREG;
	    mode = UPFILE;
	}
	else if (fd.isDirectory()) {
	    if (debug)
		System.out.print("fattr: " + file + " is a directory\n");
	    type = FT_NFDIR;
	    mode = UPDIR;
	}
	else {
	    System.err.print("fattr: " + file + " has unknown type\n");
	    type = FT_NFNON;
	    mode = 0; // don't know what kind of file system object this is
	}

	// compute the mode - everyone can access the file in the ways
	//   that the java program can.
	if (fd.canRead()) {
	    mode |= UP_OREAD | UP_GREAD | UP_WREAD;
	    mode |= UP_OEXEC | UP_GEXEC | UP_WEXEC;
	}
	if (fd.canWrite())
	    mode |= UP_OWRITE | UP_GWRITE | UP_WWRITE;
	
	// from now on assume either file or directory
	if (fd.isFile())
	    nlink = 1;
	else // directories always have 2 links
	    nlink = 2;

	uid = 0;
	gid = 0;
	// java.io.File seems to report the length of directories as 0
	//   always which upsets some clients, so make directory size 
	//   always 512.
	if (fd.isDirectory()) {
	    size = 512;
	}
	else {
	    // normal files use fd.length()
	    size = fd.length();
	}

	blocksize = 512; // XXX common value, how do I get this in java?

	rdev = 0;
	blocks = (size + blocksize - 1) / blocksize;
	fsid = 0;
	fileid = fileHandles.Allocate(file);

	// java supplies a long which is unrelated to the time except
	//   that it increases every time the file is modified.
	long lastmod = fd.lastModified();

	atime = new timeval(tm.Seconds(lastmod), tm.MilliSeconds(lastmod));
	ctime = new timeval(0, 0);
	mtime = new timeval(tm.Seconds(lastmod), tm.MilliSeconds(lastmod));
	
	if (debug) {
	    System.out.print("fattr: Mtime(" + lastmod + "): ");
	    mtime.Print();
	}

	return NFS_OK;
    };

    // there need to be a variety of set routines to set the values
    //   of these items.
    public static void main(String args[]) throws FileNotFoundException {
	PathMapper pm = new NTPathMapper();
	Handle h = new Handle("no-cache", pm);
	TimeMapper tm = new NTTimeMapper();
	fattr fa = new fattr(h, tm);

	long begin = System.currentTimeMillis();
	for (int i = 0; i < 100; i++) {
	    fa.Load(args[0]);
	}
	long end = System.currentTimeMillis();
	System.out.println("1000 iterations took " + (end - begin) + "ms");
    }
};
