import java.io.IOException;


public class LERS {
	
	static PerformLERS doLERS;
	
	public static void main(String[] args) throws IOException {

		doLERS = new PerformLERS();
		
		doLERS.readAndPrintFiles();
		doLERS.setStableAttributes(1);
		
		long tStart = System.currentTimeMillis();
		doLERS.findRules();
		long tEnd = System.currentTimeMillis() - tStart;
		
		System.out.println("Total Time :" + tEnd/1000.0);
		
	}

}
