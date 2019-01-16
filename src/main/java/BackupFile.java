import java.io.*;

class BackupFile {
    RandomAccessFile output;
    String backupFileName = "cache"; // should be a parameter

    BackupFile(Handle handles, String cacheName) {
	backupFileName = cacheName;
	if (LoadCache(handles) == false)
	    System.err.print("cache file: error loading file " + 
			     backupFileName + "\n");
	// create a way to add data to the cache file
	try {
	    output = new RandomAccessFile(backupFileName, "rw");
	} catch(IOException e) {
	    System.err.print("error opening the cache file for writing\n");
	}
    }

    boolean LoadCache(Handle handles) {
	try {
	    ParseFile(handles);
	} catch(FileNotFoundException e) {
	    System.out.print("Notice: no cache file found\n");
	} catch(IOException e) {
	    System.out.print("error reading cache file\n");
	    return false;
	}
	return true;
    }
    
    boolean ParseFile(Handle handles) throws FileNotFoundException, IOException{
	FileInputStream fi = new FileInputStream("cache");
	BufferedInputStream buf = new BufferedInputStream(fi, 8192);
	StreamTokenizer str = new StreamTokenizer(buf);
	// configure the tokenizer - allow any printable ascii char to be in
	//   the file name and ignore newlines.
	str.eolIsSignificant(false);
	str.wordChars(17, 126);

        parseLoop:
	  for (;;) { 
	      String path;
	      long value;
	      
	      //
	      // the first token should be a word which is the name of the file
	      //
	      int next = str.nextToken();
	      switch(next) {
	      case StreamTokenizer.TT_EOF:
		  if (str.lineno() > 0) 
		      System.out.print("\n");
		  System.out.print("end of cache found at line " 
				   + str.lineno() + "\n");
		  break parseLoop;
	      case StreamTokenizer.TT_NUMBER:
		  value = (long) str.nval;
		  break;
	      default:
		  System.err.print("Parse error reading handle id line " +
				   str.lineno() + "\n");
		  break parseLoop;
	      }
	    
	      //
	      // the next item should be handle number for this file name
	      //
	      next = str.nextToken();
	      switch(next) {
	      case StreamTokenizer.TT_WORD:
		  path = str.sval;
		  break;
	      default:
		  System.err.print("Parse error reading path name line " +
				   str.lineno() + "\n");
		  break parseLoop;
	      }

	      //
	      // add the item to the handle table
	      //
	      handles.Add(path, value);
	      // print out some stuff so the user doesn't get bored
	      if ((str.lineno() % 1000) == 0)
		  System.out.print(str.lineno() + " ");
	      if ((str.lineno() % (10 * 1000)) == 0)
		  System.out.print("\n");
	  }

	return true;
    }
    
    // store a binding in the backup file
    public boolean StoreItem(String path, long id) {
	try {
	    String line = new String(id + "\t" + path + "\n");
	    output.seek(output.length());
	    output.writeBytes(line);
	} catch(IOException e) {
	    System.err.print("error writing line to cache file\n");
	    return false;
	}
	return true;
    }
};
