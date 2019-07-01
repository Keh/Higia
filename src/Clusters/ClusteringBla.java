package Clusters;

import java.util.ArrayList;
import java.util.Arrays;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;

import moa.cluster.Clustering;
import moa.evaluation.F1;
import moa.evaluation.SilhouetteCoefficient;
import moa.gui.visualization.DataPoint;
import utils.InstanceKernel;
import utils.MicroCluster;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

public class ClusteringBla {

	Clustering microClusters = null;
	ArrayList<MicroCluster> windowMicroClusters = new ArrayList<>();
	ArrayList<MicroCluster> windowNewMicro = new ArrayList<>();
	F1 silhouette = new F1();

	ArrayList<DataPoint> points = new ArrayList<>();
	
	
	SummClusters algClustream = new SummClusters();
	

	public ArrayList<MicroCluster> CluStream(ArrayList<MicroCluster> windowKernel, int numMicro,
			ArrayList<String> normClasses, long timestamp, DataPoint dp) throws Exception {

		
		algClustream.maxNumKernelsOptionSetValue(numMicro);
		algClustream.prepareForUse();
		
		// read the data set and executing clustream algorithm
		int lineSize = 0;
		double data[] = null;

		for (int c = 0; c < normClasses.size(); c++) {

			int count = 0;
			for (int i = 0; i < windowKernel.size(); i++) {
				// se eh a mesma classe

				if (windowKernel.get(i).getLabel().equals(normClasses.get(c))) {
					count++;
					lineSize = windowKernel.get(i).getCenter().length - 1;
					data = new double[lineSize];

					for (int j = 0; j < lineSize; j++) {
						data[j] = windowKernel.get(i).getCenter()[j];
					}

					Instance inst = new DenseInstance(1, data);

					points.add(dp);

					algClustream.trainOnInstanceImpl(inst);
					windowKernel.remove(i);
					i--;

				}
			}

			// obtain the micro-clusters
			microClusters = algClustream.getMicroClusteringResult();

//			silhouette.evaluateClusteringPerformance(microClusters, microClusters, points);
//			
//			System.out.println(silhouette.getLastValue(0));

			// remove too small clusters
			for (int j = 0; j < microClusters.size(); j++) {
//				System.out.println(microClusters.get(j).getWeight());
				if ((microClusters.get(j)).getWeight() <= 2) {
					microClusters.remove(j);
					j--;
				}
			}

			// test information
			for (int i = 0; i < microClusters.size(); i++) {
				
				MicroCluster mdtemp = new MicroCluster((InstanceKernel) microClusters.get(i), normClasses.get(c),
						"normal", timestamp);

//				System.out.println("N " + mdtemp.getN());
//				System.out.println("radius " + mdtemp.getRadius());
//				System.out.println(Arrays.toString(mdtemp.getCenter())+ " " + mdtemp.getLabel() );
//				System.out.println("bla " + mdtemp.getLabel());
//				System.out.println("bla " + mdtemp.getType());

				windowMicroClusters.add(mdtemp);
			}

		}

		return windowMicroClusters;
	}

	public ArrayList<MicroCluster> CluStreamOnline(ArrayList<MicroCluster> windowKernel, float numMicro, long timestamp)
			throws Exception {

		algClustream.prepareForUse();

		// read the data set and executing clustream algorithm
		int lineSize = 0;
		double data[] = null;

		int count = 0;
		for (int i = 0; i < windowKernel.size(); i++) {
			// se eh a mesma classe

			count++;
			lineSize = windowKernel.get(i).getCenter().length;
			data = new double[lineSize];

			for (int j = 0; j < lineSize; j++) {
				data[j] = windowKernel.get(i).getCenter()[j];
			}

			Instance inst = new DenseInstance(1, data);


			algClustream.trainOnInstanceImpl(inst);
			windowKernel.remove(i);
			i--;

		}

		// obtain the micro-clusters
		microClusters = algClustream.getMicroClusteringResult();
		
		// remove too small clusters
		for (int j = 0; j < microClusters.size(); j++) {
			if ((microClusters.get(j)).getWeight() < 3) {
				microClusters.remove(j);
				j--;
			}
		}

		// test information
		for (int i = 0; i < microClusters.size(); i++) {

			MicroCluster mdtemp = new MicroCluster((InstanceKernel) microClusters.get(i), "", "", timestamp);

			windowNewMicro.add(mdtemp);
		}

		return windowNewMicro;
	}
	
	

	public ArrayList<MicroCluster> onlineClustering(MicroCluster inst, long timestamp, double val) {

		Instance inst2 = new DenseInstance(1, inst.getCenter());
		algClustream.trainOnInstanceImpl(inst2);
		microClusters = algClustream.getMicroClusteringResult();
		this.windowNewMicro.clear();
		for (int i = 0; i < microClusters.size(); i++) {

			MicroCluster mdtemp = new MicroCluster((InstanceKernel) microClusters.get(i), Double.toString(val),
					"normal", timestamp);

//			System.out.println("N " + mdtemp.getN());
//			System.out.println("radius " + mdtemp.getRadius());
//			System.out.println("centro " + Arrays.toString(mdtemp.getCenter()));
//			System.out.println("bla " + mdtemp.getLabel());
//			System.out.println("bla " + mdtemp.getType());

			windowNewMicro.add(mdtemp);
		}
		return windowNewMicro;

	}
	
	public int getTimeWindow() {
		
		return algClustream.timeWindowOption.getValue();
	}
	
	
	

}
