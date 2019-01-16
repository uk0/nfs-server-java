// this class is used to encapsulate all of the file system information.  The
//   idea is that it be replaced by native code to get better NFS behavior.
class FileSystemInfo {
    
    FileSystemInfo() { };
    
    void SetFS(String path) { /* nothing */ };
    
    long TransferSize() { return 8192; }
    long BlockSize()    { return 512; }
    long TotalBlocks()  { return 0; }
    long FreeBlocks()   { return 0; }
    long AvailableBlocks() { return 0; }
}
