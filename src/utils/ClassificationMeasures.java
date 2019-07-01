package utils;

public class ClassificationMeasures {
	public String name = null;
	int n = 0;
	int classified = 0;
	
	ClassificationMeasures(){
		
	}
	public ClassificationMeasures(String name){
		this.name = name;
		this.n = 1;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public int getClassified() {
		return classified;
	}
	public void setClassified(int classified) {
		this.classified = classified;
	}
	
	

}
