import java.io.*;

class NTTimeMapper extends TimeMapper {
    // NT stores time in 100 ns incriments since 1600.  Convert this to
    //   seconds and milliseconds since 1970;

    // computed constant - the NT local time for jan 1 1970
    long startTime;
    
    NTTimeMapper() {
	// gotta load in a 64 bit number
	startTime = 27111902 << 32 + 54590 << 16 + 32768;
    }
    
    long Seconds(long lt) {
	long delta = lt - startTime;
	delta /= 1000;
	return delta;
    }
    long MilliSeconds(long lt) {
	long delta = lt - startTime;
	delta %= 1000;
	return delta;
    }
};
 
