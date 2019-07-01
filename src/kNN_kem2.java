
/*
 *    kNN.java
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Random;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.classifiers.lazy.neighboursearch.KDTree;
import moa.classifiers.lazy.neighboursearch.LinearNNSearch;
import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;
import moa.core.Measurement;
import moa.gui.visualization.DataPoint;
import utils.ConDis;
import utils.DriftEvolution;
import utils.InstanceKernel;
import utils.MicroCluster;
import utils.NearestNeighbours;
import Clusters.ClusteringBla;
import Clusters.SummClusters;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.MultiChoiceOption;

/**
 * k Nearest Neighbor.
 * <p>
 *
 * Valid options are:
 * <p>
 *
 * -k number of neighbours <br>
 * -m max instances <br>
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version 03.2012
 */
public class kNN_kem2 extends AbstractClassifier implements MultiClassClassifier {

	private static final long serialVersionUID = 1L;

	private boolean initialized;
	private boolean newInitialized;

	public IntOption kOption = new IntOption("k", 'k', "The number of neighbors", 10, 1, Integer.MAX_VALUE);

	public IntOption limitOption = new IntOption("limit", 'w', "The maximum number of instances to store", 1000, 1,
			Integer.MAX_VALUE);

	public int limit_Option = 1000;

	protected double prob;

	public MultiChoiceOption nearestNeighbourSearchOption = new MultiChoiceOption("nearestNeighbourSearch", 'n',
			"Nearest Neighbour Search to use", new String[] { "LinearNN", "KDTree" },
			new String[] { "Brute force search algorithm for nearest neighbour search. ",
					"KDTree search algorithm for nearest neighbour search" },
			0);

	ArrayList<String> classes;

	int C = 0;

	@Override
	public String getPurposeString() {
		return "kNN: special.";
	}

	protected Instances window;
	long timestamp = -1;

	double threshold = 1.1;
	double kernel = 1;
	double maxClusters = 0;
	double learningRate = 0.5;

	// this is just a test
	// double kernel

	ArrayList<MicroCluster> windowKernel;
	ArrayList<MicroCluster> neWindow;
	ArrayList<ConDis> conSeDist;
	ArrayList<DriftEvolution> driftList;

	SummClusters clusters;
	ClusteringBla clusteringBla;

	BufferedWriter output = null;

	@Override
	public void setModelContext(InstancesHeader context) {
		try {
			this.classes = new ArrayList<>();
			this.windowKernel = new ArrayList<>();
			this.neWindow = new ArrayList<>();
			this.conSeDist = new ArrayList<>();
			this.driftList = new ArrayList<>();
			this.window = new Instances(context, 0);
			this.window.setClassIndex(context.classIndex());

			clusteringBla = new ClusteringBla();

			try {
				File file = new File("/home/kemilly/example.txt");
				if (file.exists())
					file.delete();
				output = new BufferedWriter(new FileWriter(file));

			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.err.println("Error: no Model Context available.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void resetLearningImpl() {
		this.windowKernel = null;
		this.neWindow = null;
		this.initialized = false;
		this.newInitialized = false;
		this.classes = null;
		clusters = new SummClusters();
	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {

		if (inst.classValue() > C)
			C = (int) inst.classValue();

		if (this.window == null) {
			this.window = new Instances(inst.dataset());
		}
		// remove instances
		if (this.limitOption.getValue() <= this.window.size()) {
			this.window.delete(0);
		}
		this.window.add(inst);
	}

	public void setLimit_Option(int size) {
		this.limit_Option = size;
	}

	public void setLearningRate(double rate) {
		this.learningRate = rate;
	}

	public void trainOnInstance(MicroCluster inst, int minClusters, DataPoint dp) throws Exception {
		timestamp++;

		int lineSize = inst.getCenter().length - 1;
		double[] data = new double[lineSize];

		for (int j = 0; j < lineSize; j++) {
			data[j] = inst.getCenter()[j];
		}

		// window is empty -> initial training phase
		if (!initialized) {

			// amount of classes before test phase
			if (classes.indexOf(inst.getLabel()) == -1) {
				classes.add(inst.getLabel());
			}

			// first elements of the window
			if (this.windowKernel.size() < this.limit_Option) {
				this.windowKernel.add(inst);

			} else {

				initialized = true;

				// reduce prototypes
				ArrayList<MicroCluster> micros = clusteringBla.CluStream(this.windowKernel, minClusters, classes,
						timestamp, dp);
				this.windowKernel.clear();
				this.windowKernel = micros;
				this.maxClusters = this.windowKernel.size();
				System.out.println(maxClusters);
//				this.maxClusters = this.maxClusters * 1.2;

//				for (int i = 0; i < maxClusters; i++)
//					this.driftList.add(null);

			}
		}

	}

	public void modelUpdate() throws IOException {

		// 3.2 Merge closest two kernels
		int closestA = 0;
		int closestB = 0;
		double minDistance = Double.MAX_VALUE;

		double radiusB = 0;
		double radiusA = 0;
		for (int i = 0; i < this.windowKernel.size(); i++) {
			double[] centerA = this.windowKernel.get(i).getCenter();
			radiusA = this.windowKernel.get(i).getRadius();
			for (int j = i + 1; j < this.windowKernel.size(); j++) {
				double dist = distance(centerA, this.windowKernel.get(j).getCenter());
				radiusB = this.windowKernel.get(j).getRadius();
				if (dist < minDistance) {
					minDistance = dist;
					closestA = i;
					closestB = j;
				}
			}

			assert (closestA != closestB);
			// heuristica
			if (minDistance <= (radiusA + radiusB)) {
//				System.out.println("acontece");
				this.windowKernel.get(closestA).add(this.windowKernel.get(closestB));
//				this.windowKernel.remove(closestB);
			}

		}
//		System.out.println(" " + this.windowKernel.size());

	}

	public void removeClusters() {

		long threshold = timestamp - clusteringBla.getTimeWindow();
		for (int i = 0; i < this.windowKernel.size(); i++) {
//			if(this.windowKernel.get(i).getVeloc() == 0) {
//				this.windowKernel.remove(i);				
//			} 			
			if (this.windowKernel.get(i).getRelevanceStamp() <= threshold) {
				if (this.windowKernel.size() > (clusteringBla.getTimeWindow() / 2)) {

					this.windowKernel.remove(i);
				}
			}

		}

	}

	public int classify(InstanceKernel inst, ArrayList<String> claNormal, long time) throws Exception {
//		an unknown instance arrive from the stream
		timestamp = time;

		int lineSize = inst.getCenter().length - 1;
		double[] data = new double[lineSize];

		for (int j = 0; j < lineSize; j++) {
			data[j] = inst.getCenter()[j];
		}

		Instance inst2 = new DenseInstance(1, data);

		InstanceKernel inKe = new InstanceKernel(inst2, inst2.numAttributes(), timestamp);

//		// get kOption neighbours 
		ArrayList<NearestNeighbours> neighbours = kClosestNeighbor(inKe, kOption.getValue());

		String[] info = testInstance(inKe, neighbours, inst.getCenter()[lineSize]);

		output.write("true " + Double.toString(inst.getCenter()[lineSize]));
		output.write(" class , " + info[0]);
		output.write(" type , " + info[1]);
		output.newLine();

		if (info[0].equals(Double.toString(inst.getCenter()[lineSize]))) {
			return 1;
		}

		// if unknown SUSPEITO
		if (info[0].equals("")) {
			return 2;
		}

		return 0;
	}

	// ver se eh novidade ou extensao
	public void detectingThings(InstanceKernel in) throws Exception {

		int minClusters = 50;
		int minNovelty = 1000;
		int sim = 0;

//		System.out.println("neWindow " + this.neWindow.size());
//		System.out.println("windowkernel " + this.windowKernel.size());

		// window is empty -> initial training phase

		if (this.neWindow.size() < minNovelty) {
			MicroCluster micro = new MicroCluster(in, "", "anormal", timestamp);
			this.neWindow.add(micro);

		} else {
			if (!newInitialized) {

				ArrayList<MicroCluster> micros2 = clusteringBla.CluStreamOnline(this.neWindow, minClusters, timestamp);

				this.neWindow.clear();
				this.neWindow = micros2;
				newInitialized = true;

			} else {
//				setThreshold(-0.1);

				Instance den = new DenseInstance(1, in.getCenter());

				double minDistance = Double.MAX_VALUE;
				InstanceKernel closestKernel = null;

				for (int i = 0; i < this.neWindow.size(); i++) {

					double dis = distance(this.neWindow.get(i).getCenter(), in.getCenter());

					if (dis <= minDistance) {

						closestKernel = this.neWindow.get(i);
						minDistance = dis;
					}
				}

				// 2. Check whether instance fits into closestKernel
				double radius = 0.0;
				if (closestKernel.getWeight() == 1) {
					// Special case: estimate radius by determining the distance to the
					// next closest cluster
					radius = Double.MAX_VALUE;
					double[] center = closestKernel.getCenter();
					for (int i = 0; i < this.neWindow.size(); i++) {
						if (this.neWindow.get(i) == closestKernel) {
							continue;
						}

						double distance = distance(this.neWindow.get(i).getCenter(), center);
						radius = Math.min(distance, radius);
					}
				} else {
					radius = closestKernel.getRadius();
				}

				if (minDistance < radius) {
					// Date fits, put into kernel and be happy
					closestKernel.insert(den, timestamp);
					sim = 1;
//					return;
				}
				if (sim == 0) {

					// 3. Date does not fit, we need to free
					// some space to insert a new kernel
					long threshold = timestamp - 1000; // Kernels before this can be forgotten

					// 3.1 Try to forget old kernels
					for (int i = 0; i < this.neWindow.size(); i++) {
						if (this.neWindow.get(i).getRelevanceStamp() < threshold) {
							MicroCluster element = new MicroCluster(in, "", "anormal", timestamp);
							this.neWindow.set(i, element);
							sim = 1;
//							 return;
						}
					}
				}

				if (sim == 0) {

					// 3.2 Merge closest two kernels
					int closestA = 0;
					int closestB = 0;
					minDistance = Double.MAX_VALUE;
					for (int i = 0; i < this.neWindow.size(); i++) {
						double[] centerA = this.neWindow.get(i).getCenter();
						for (int j = i + 1; j < this.neWindow.size(); j++) {
							double dist = distance(centerA, this.neWindow.get(j).getCenter());
							if (dist < minDistance) {
								minDistance = dist;
								closestA = i;
								closestB = j;
							}
						}
					}
					assert (closestA != closestB);

					this.neWindow.get(closestA).add(this.neWindow.get(closestB));
					MicroCluster element = new MicroCluster(in, "", "anormal", timestamp);
					this.neWindow.set(closestB, element);
				}

				for (int i = 0; i < this.neWindow.size(); i++) {
					double newClass = classes.size();

					if (this.neWindow.get(i).getN() >= 10) {

						minDistance = Double.MAX_VALUE;
						int indiceCluster = 0;
						for (int j = 0; j < this.windowKernel.size(); j++) {
							double dist = distance(this.windowKernel.get(j).getCenter(),
									this.neWindow.get(i).getCenter());
							if (dist < minDistance) {
								minDistance = dist;
								indiceCluster = j;
							}
						}
						if (minDistance <= (this.windowKernel.get(indiceCluster).getRadius() * threshold)) {
							newClass = Double.parseDouble(this.windowKernel.get(indiceCluster).getLabel());
						} else {
							classes.add(Double.toString(newClass));
						}

						this.neWindow.get(i).setLabel(Double.toString(newClass));
						this.neWindow.get(i).setType("novelty");

						this.windowKernel.add(this.neWindow.get(i));

						removeClusters();

						this.neWindow.remove(i);
					}

				}

			}

			if (this.neWindow.size() > 1000) {
				this.neWindow.remove(0);
			}
		}

	}

	// get distance from k closest windowKernel
	public ArrayList<NearestNeighbours> kClosestNeighbor(InstanceKernel inst, int kOption) {

		MicroCluster closestKernel = null;

		NearestNeighbours nN = null;
		NearestNeighbours[] votes = new NearestNeighbours[kOption];

		double minDistance = Double.MAX_VALUE;
		Random rand = new Random();
		// first koption distances
		for (int i = 0; i < votes.length; i++) {
			int n = rand.nextInt(this.windowKernel.size());
			double dis = distance(inst.getCenter(), this.windowKernel.get(n).getCenter());
			votes[i] = new NearestNeighbours(i, dis, this.windowKernel.get(n));
		}
		for (int i = 0; i < this.windowKernel.size(); i++) {
			double distance = distance(inst.getCenter(), this.windowKernel.get(i).getCenter());
			int first = 0;
			double maxDistance = Double.MIN_VALUE;
			for (int j = 0; j < votes.length; j++) {
				if (votes[j].getDistance() > maxDistance) {
					maxDistance = votes[j].getDistance();
					first = j;
				}
			}

			if (distance < votes[first].getDistance()) {
				closestKernel = this.windowKernel.get(i);
				nN = new NearestNeighbours(i, distance, closestKernel);
				votes[first] = nN;
			}

		}

		ArrayList<NearestNeighbours> votesList;
		votesList = new ArrayList<>(Arrays.asList(votes));

		return votesList;
	}

	// classification phase
	public String[] testInstance(InstanceKernel inst, ArrayList<NearestNeighbours> neighbours, double realLabel)
			throws Exception {

		// por voto, obtem-se a quantidade para cada label de acordo com a distancia.
		double[] votes = new double[classes.size()];
		String[] info = new String[2];
		info[0] = "";
		info[1] = "";
		double minDist = Double.MAX_VALUE;
		int index = 0;

		Random rand = new Random();
		int aux = 0;

		// real classification
		for (int i = 0; i < neighbours.size(); i++) {

			double foo = Double.parseDouble(neighbours.get(i).getKernel().getLabel());
//			// if the dist is less or equal to the radius of the closest cluster
			votes[(int) foo]++;

			// minimal dist to the closets cluster
			double dist = distance(inst.getCenter(), neighbours.get(i).getKernel().getCenter());

			if (dist < minDist) {
				minDist = dist;
				index = neighbours.get(i).getIndex();
				aux = i;
			}

		}

		// majority vote
		int valor = (kOption.getValue() / 2) + 1;

//		System.out.println(Arrays.toString(votes) + "---" + realLabel);
//		if(this.driftList.get(index) !=null) {
//			threshold = this.driftList.get(index).getTreshold();
//		} else {
//			threshold = 1.1;
//		}

		// update closets cluster
		if (minDist <= (this.windowKernel.get(index).getRadius() * threshold)) {

//			if(this.driftList.get(index)==null) {
////				System.out.println(index);
//				DriftEvolution element = new DriftEvolution(windowKernel.get(index).getCenter(), index, (int)windowKernel.get(index).getN(), (int)windowKernel.get(index).getTime());
////				DriftEvolution element = new DriftEvolution(windowKernel.get(index), index);
//				this.driftList.set(index, element);
//			}else {
////				System.out.println(this.driftList.get(index).getIdCluster());
//			}
			
			
			// Double.toString(val); higher score
			info[0] = Double.toString(max(votes));
			info[1] = this.windowKernel.get(index).getType();


			
//			this.windowKernel.get(index).insert(inst.getCenter(), (int) timestamp);
			DriftEvolution element = new DriftEvolution(windowKernel.get(index), index);
			this.driftList.add(element);
//			this.windowKernel.get(index).sum(inst.getCenter(), learningRate);
			return info;
			
//			this.windowKernel.get(index).sum(inst.getCenter(), learningRate);
//			this.threshold += 0.00001;
			

//			return info;
		}
//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		int count  = 0;
		System.out.println("before " +  this.driftList.size());
		if(timestamp % 1000 == 0) {
			for(int i = 0; i < this.driftList.size(); i++) {
				for(int j= i+1; j < this.driftList.size(); j++) {
					if(this.driftList.get(i).getIdCluster() == this.driftList.get(j).getIdCluster()) {
						Instance in = new DenseInstance((Instance)this.driftList.get(j).getInstaceKernel());
						this.driftList.get(i).getInstaceKernel().insert(in, (long)1);
						this.driftList.remove(j);
						j--;
					}
				}
//				if(this.driftList.get(i) != null) {
//					System.out.println("id " + this.driftList.get(i).getIdCluster());
//					System.out.println("** " + Arrays.toString(this.driftList.get(i).getCenter()));
//					System.out.println("** " + this.driftList.get(i).getOldN());
////					System.out.println("** " + Arrays.toString(this.driftList.get(i).getInstaceKernel().getCenter()));
////					System.out.println("** " + this.driftList.get(i).getInstaceKernel().getN());
//					System.out.println("-- " + this.windowKernel.get(this.driftList.get(i).getIdCluster()).getN());
//					System.out.println("-- " + Arrays.toString(this.windowKernel.get(this.driftList.get(i).getIdCluster()).getCenter()));
////					System.out.println(Arrays.toString(this.driftList.get(i).getInstaceKernel().getCenter()));
////					System.out.println(this.driftList.get(i).getIdCluster());
//					count++;
//				}
			}
//			System.out.println(count);
			this.driftList.clear();
		}
		System.out.println("after " +  this.driftList.size());
//		// if majority
//		if (votes[max(votes)] >= valor) {
//			
//		}

		if (info[0] == "") { // when is false.
//			// buffer
//			detectingThings(inst);
//			this.threshold -= 0.00001;
			info[0] = "";
			info[1] = "";
		}

//		// test baseline
		info[0] = Double.toString(max(votes));
		info[1] = "normal";
//		System.out.println("threshold " + threshold);
		return info;

	}

	public void setThreshold(double valor) {
		this.threshold = threshold + valor;
		System.out.println("threshold " + threshold);
	}

	public double[] getVotesForInstance(InstanceKernel inst) {

		Instance in = new DenseInstance((Instance) inst);

		double v[] = new double[C + 1];
		try {
			NearestNeighbourSearch search;
			if (this.nearestNeighbourSearchOption.getChosenIndex() == 0) {
				search = new LinearNNSearch(this.window);
			} else {
				search = new KDTree();
				search.setInstances(this.window);
			}
			if (this.window.numInstances() > 0) {
				Instances neighbours = search.kNearestNeighbours(in,
						Math.min(kOption.getValue(), this.window.numInstances()));
				for (int i = 0; i < neighbours.numInstances(); i++) {
					v[(int) neighbours.instance(i).classValue()]++;
				}
			}
		} catch (Exception e) {
			return new double[classes.size()];
		}
		return v;
	}

	@Override
	public double[] getVotesForInstance(Instance inst) {

		double v[] = new double[C + 1];
		try {
			NearestNeighbourSearch search;
			if (this.nearestNeighbourSearchOption.getChosenIndex() == 0) {
				search = new LinearNNSearch(this.window);
			} else {
				search = new KDTree();
				search.setInstances(this.window);
			}
			if (this.window.numInstances() > 0) {
				Instances neighbours = search.kNearestNeighbours(inst,
						Math.min(kOption.getValue(), this.window.numInstances()));
				for (int i = 0; i < neighbours.numInstances(); i++) {
					v[(int) neighbours.instance(i).classValue()]++;
				}
			}
		} catch (Exception e) {
			return new double[inst.numClasses()];
		}
		return v;
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		return null;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {
	}

	public boolean isRandomizable() {
		return false;
	}

	// max value
	public int max(double[] L) {
		double max = L[0];
		int index = 0;
		for (int i = 0; i < L.length; i++) {
			if (L[i] > max) {
				max = L[i];
				index = i;
			}
		}
		return index;
	}

	// min value
	public double min(double[] L) {
		double min = L[0];
		for (int i = 0; i < L.length; i++)
			if (L[i] < min)
				min = L[i];
		return min;
	}

	private static double distance(double[] pointA, double[] pointB) {
		double distance = 0.0;
		for (int i = 0; i < pointA.length; i++) {
			double d = pointA[i] - pointB[i];
			distance += d * d;
		}
		return Math.sqrt(distance);
	}

	public void stopFile() throws IOException {
		output.close();
	}
}