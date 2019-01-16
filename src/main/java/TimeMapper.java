import java.io.*;

class TimeMapper {
    long Seconds(long localTime) {
	return localTime / (1 << 32);
    }
    long MilliSeconds(long localTime) {
	return localTime % (1 << 32);
    }
};
