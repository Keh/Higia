package utils;

public class NearestNeighbours {
	
	int index;
	double distance;
	MicroCluster kernel;
	
	public NearestNeighbours(int i, double minDistance, MicroCluster ik) {
		this.index = i;
		this.distance = minDistance;
		this.kernel = ik;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public MicroCluster getKernel() {
		return kernel;
	}

	public void setKernel(MicroCluster kernel) {
		this.kernel = kernel;
	}
	
	
	

}
