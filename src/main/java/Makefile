# interfaces used to install constants in other classes
CONSTS = rpcconsts.java \
	PortMapConst.java \
	mountdconsts.java \
 	nfsconsts.java \

# utility classes that don't rely on other NFS facilities
UTILS = TimeMapper.java \
	NTTimeMapper.java \
	timeval.java \
	UnixPermissions.java \
	sort.java \
	UDPPacketPort.java \
	PacketCollector.java

# utilities that rely on other NFS facilities like the constants defined above
NFSUTILS = fattr.java \
	BackupFile.java \
	Handle.java \
	MTList.java \
	PathMapper.java \
	NTPathMapper.java \
	XDRPacket.java \
	FileSystemInfo.java \
	fhandle.java \
	Exports.java \
	NFSIO.java \
	NFSDir.java \
	NFSException.java \
	XIDCache.java

# classes that handle collecting and dispatching rpc calls
RPC = rpchandler.java \
	rpcmanager.java \
	portmaphandler.java \
	mountdhandler.java \
	NFSHandler.java \
	portmap.java

NFSTHREADS = NFSItem.java \
	NFSOperation.java \
	NFSMTHandler.java
	

# The nfs servers - nfs.java is the main server, the others supply nfs with
#   platform specific modules like the path mapper and the time mapper.
#   To port to a new platform copy and edit one of the platform specific
#   nfs's (like ntnfs) and add it here.
NFS = nfs.java \
	ntnfs.java \
	unixfs1.java

SOURCES = $(CONSTS) $(UTILS) $(NFSUTILS) $(RPC) $(NFSTHREADS) $(NFS)
OBJECTS = $(addsuffix .compiled,$(basename $(SOURCES)))

JAVAC = javac
JFLAGS = -g

all: $(OBJECTS)

$(OBJECTS): %.compiled: %.java
	$(JAVAC) $(JFLAGS) $<
	@echo > $@

lines:
	@wc -l $(SOURCES)

tar:
	tar cf jnfs.tar Makefile $(SOURCES)

zip:
	zip nfs.zip $(addsuffix .class,$(basename $(SOURCES)))
