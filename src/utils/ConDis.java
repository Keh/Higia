package utils;

import java.util.Arrays;

public class ConDis {
	
	double[] inst;
	double distance;
	String label;
	
	public ConDis(double[] inst, double dis, String label) {
		this.inst = inst;
		this.distance = dis;
		this.label = label;
	}
	public ConDis(double[] inst) {
		this.inst = inst;
	}

	public double[] getInst() {
		return inst;
	}

	public void setInst(double[] inst) {
		this.inst = inst;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public String toString() {
		return "ConDis [inst=" + Arrays.toString(inst) + ", distance=" + distance + ", label=" + label + "]";
	}
	
	
	

}
