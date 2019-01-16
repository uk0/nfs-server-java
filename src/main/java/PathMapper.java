interface PathMapper {
    String MakePath(String directory, String component);
    String Canonicalize(String path); // maps path to canonical one
    String Convert(String unixPath);  // converts UNIX path to local path
};
