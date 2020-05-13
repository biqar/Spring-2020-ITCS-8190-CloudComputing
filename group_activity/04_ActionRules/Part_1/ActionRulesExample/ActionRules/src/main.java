import java.io.IOException;


public class main {
	static PerformLERS performLERS;
	static GenerateActionRules generateActionRules;
	
	public static void main(String[] args) throws IOException {
		
		
		performLERS = new PerformLERS();
		generateActionRules = new GenerateActionRules(performLERS); 
		
		performLERS.readAndPrintFiles();
		
		performLERS.setStableAttributes(1);
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				performLERS.findRules();
				
			}
		});
		t.start();
		
//		generateActionRules.getDecisionFromValue();
		generateActionRules.setMinSupportAndConfidence();
		
		long tStart = System.currentTimeMillis();
//		performLERS.findRules();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generateActionRules.generateActionRules();
		
		long tEnd = System.currentTimeMillis() - tStart;
		
		System.out.println("\n\nTotal Time :" + tEnd/1000.0);

	}

}
