import java.util.*;
import java.io.*;

class Sort {
    boolean verifyResults; // should sorts make sure list is sorted at end

    Sort() {
	// by default don't spend time verifying resulting list
	verifyResults = false;
    }
    void VerifyResults(boolean flag) {
	verifyResults = flag;
    }

    void BubbleSort(String [] list) {
	if (list == null)
	    return;
	BubbleSort(list, 0, list.length - 1);

	if (verifyResults) {
	    // make sure the result is correct
	    for (int i = 0; i < list.length - 1; i++) {
		if (list[i].compareTo(list[i + 1]) > 0) 
		  System.err.print("bubblesort sequence error " + list[i] + " " 
				   + list[i + 1] + "\n");
	    }
	}
    }
    void BubbleSort(String [] list, int begin, int end) {
	// if there are 0 or 1 elements in the list, don't try to sort it
	if ((list == null) || ((end - begin) <= 1))
	    return;
	
	boolean changed;
	do {
	    changed = false;
	    for (int i = begin; i < end; i++) {
		// if the element after list[i] should come after it, swap
		if (list[i].compareTo(list[i + 1]) > 0) {
		    String save = list[i + 1];
		    list[i + 1] = list[i];
		    list[i] = save;
		    changed = true;
		}
	    }
	} while (changed);
    }

    void MinSort(String [] list) {
	System.out.print("min sorting " + list.length + " files\n");
	MinSort(list, 0, list.length - 1);
    }
    void MinSort(String [] list, int begin, int end) {
	for (int i = 0; i < end; i++) {
	    for (int j = i; j <= end; j++) {
		if (list[i].compareTo(list[j]) > 0) {
		    String save = list[j];
		    list[j] = list[i];
		    list[i] = save;
		}
	    }
	}
    }

    void quicksort(String list[]) {
	System.out.print("quick sorting " + list.length + " files\n");
	quicksort(list, 0, list.length - 1);

	if (verifyResults) {
	    // make sure the result is correct
	    for (int i = 0; i < list.length - 1; i++) {
		if (list[i].compareTo(list[i + 1]) > 0) 
		  System.err.print("quicksort sequence error " + list[i] + " " 
				   + list[i + 1] + "\n");
	    }
	}
    }
    void quicksort(String list[], int begin, int end) {
	if (end == begin)
	    return;
	else if ((end - begin) < 5) {
	    BubbleSort(list, begin, end);
	    return;
	}

	String ref = list[(begin + end) / 2];
	int bottom = begin, top = end;

	// find first element from top that is too small for top 1/2, first
	//  element from bottom that is too big for bottom 1/2 and swap them.
	for (;;) {
	    while (bottom < end && ref.compareTo(list[bottom]) >= 0)
	        bottom++;
	    while (top > begin && ref.compareTo(list[top]) <= 0)
	        top--;
	    if (top <= bottom)
	        break;

	    String save = list[top];
	    list[top] = list[bottom];
	    list[bottom] = save;
	}

	if (bottom == end || top == begin) {
	    // picked a bad pivot - bubble sort the list
	    BubbleSort(list, begin, end);
	}
	else {
	    quicksort(list, begin, top);
	    quicksort(list, bottom, end);
	}
    }
    
    public static void main(String args[])
      throws FileNotFoundException, IOException {
	File fd = new File(args[0]);
	String [] files;
	Sort sorter = new Sort();
	sorter.VerifyResults(true);

	files = fd.list();
	Date begin = new Date();
	sorter.BubbleSort(files);
	Date end = new Date();
	System.out.print("bubble " + (end.getTime() - begin.getTime())
			 + "ms\n");

	//files = fd.list();
	//begin = new Date();
	//sorter.MinSort(fd.list());
	//end = new Date();
	//System.out.print("min " + (end.getTime() - begin.getTime()) + "ms\n");

	files = fd.list();
	begin = new Date();
	sorter.quicksort(files);
	end = new Date();
	System.out.print("qsort " + (end.getTime() - begin.getTime()) 
			 + "ms\n");
    }
};
