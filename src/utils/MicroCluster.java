package utils;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;

@SuppressWarnings("serial")
public class MicroCluster extends InstanceKernel{

	private String label;	  // label of the class associated to the micro-cluster. If it is a novelty the class is a sequential number
    private String type;  	  // category: normal, novelty or extension
    private long time;		      // timestamp associated to the last example classified in this micro-cluster
    private double threshold;
    
    public MicroCluster(InstanceKernel element, String labelClasse, String category, long time){
    	super(element);
    	this.label = labelClasse;
    	this.type = category;
    	this.time = time;
    	this.threshold = 1.1;
    }  
    
    public void insert(double element[], int timestamp){
    	Instance inst= new DenseInstance(1, element);	
    	super.insert(inst, timestamp);
    	this.setTime(timestamp);
    }
                
    public void setType(String category){
		this.type = category;
	}
            								
	public void setLabel(String labelClass){
			this.label = labelClass;
	}
            
	public String getType(){
		return type;
	}
            							
	public String getLabel(){
		return label;
	}

	public long getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}	
			
}	
		
		
