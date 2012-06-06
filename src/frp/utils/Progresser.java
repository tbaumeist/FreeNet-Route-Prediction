package frp.utils;

import java.io.PrintStream;

public class Progresser {
	private PrintStream outStream;
	private int totalCount;
	private int currentCount = 0;
	
	private final int DISPLAY_COUNT = 50;
	
	public Progresser(PrintStream out, int totalCount){
		this.outStream = out;
		this.totalCount = totalCount;
	}
	
	public void hit(){
		if(currentCount >= this.totalCount)
			return;
		
		if(currentCount ==0)
			printBanner();
		
		int before = displayCount(currentCount);
		currentCount++;
		int after = displayCount(currentCount);
		
		int diff = after - before;
		for(int i = 0; i < diff; i++)
			this.outStream.print(".");
		
		if(currentCount == this.totalCount)
			this.outStream.println();
	}
	
	private void printBanner(){
		this.outStream.print("\t");
		for(int i = 0 ; i < this.DISPLAY_COUNT;i++)
			this.outStream.print("_");
		this.outStream.print("\n\t");
	}
	
	private int displayCount(int currentHit){
		if(currentHit == 0)
			return 0;
		
		double percent = currentHit / (double)this.totalCount;
		double progress = percent * this.DISPLAY_COUNT;
		return (int)Math.floor(progress);
	}
	
}
