package utils;

public class DriftEvolution {

	InstanceKernel instaceKernel;
	int idCluster;
	String type;
	double treshold = 1.1;
	double[] center;
	int oldN;
	int oldTime;

	public DriftEvolution(InstanceKernel instaceKernel, int idCluster, String type) {
		this.instaceKernel = instaceKernel;
		this.idCluster = idCluster;
		this.type = type;
	}
	
	public DriftEvolution(InstanceKernel instaceKernel, int idCluster) {
		this.instaceKernel = instaceKernel;
		this.idCluster = idCluster;
	}
	
	public DriftEvolution(double[] centre, int idCluster, int N, int time) {
		this.center = centre;
		this.idCluster = idCluster;
		this.oldN = N;
		this.oldTime = time;
	}

	public DriftEvolution() {

	}
	

	public InstanceKernel getInstaceKernel() {
		return instaceKernel;
	}

	public void setInstaceKernel(InstanceKernel instaceKernel) {
		this.instaceKernel = instaceKernel;
	}

	public int getIdCluster() {
		return idCluster;
	}

	public void setIdCluster(int idCluster) {
		this.idCluster = idCluster;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getTreshold() {
		return treshold;
	}

	public void setTreshold(double treshold) {
		this.treshold = treshold;
	}

	public double[] getCenter() {
		return center;
	}

	public void setCenter(double[] center) {
		this.center = center;
	}

	public int getOldN() {
		return oldN;
	}

	public void setOldN(int oldN) {
		this.oldN = oldN;
	}

	public int getOldTime() {
		return oldTime;
	}

	public void setOldTime(int oldTime) {
		this.oldTime = oldTime;
	}

}
