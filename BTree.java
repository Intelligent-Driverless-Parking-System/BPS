// Feb22, left node does not make copy of flag matrix from parent
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

public class BTree{
	private ArrayList<Integer> garage_capacity = new ArrayList<Integer>();
	private ArrayList<Garage> garages = new ArrayList<Garage>();
	private ArrayList<Car> pending_vehicles = new ArrayList<Car>();
	public static int CAPACITY = 16; //<-- change this
//	public static final int CAPACITY_VAR = 1;
	public static double LAMBDA = 1.0; //<-- change this
	private int goal = 0;
	private int numGarages = 4;
	private int numCars;
	private static int nonEmptyGarages;
//    private boolean leftTruncation;
    private static int total_capacity;
    private int alpha = 1;
	private double eta = 1;
	private int beta = 0;
	private double zeta = 0;
    public static double total_cost_diff = 0.0;
    public static int num_samples = 0;
    public static int iterations = 0;
    public static double total_distance_diff = 0.0;
	private Node root;
	double costMatrix[][];
	//double costMatrixSorted[][];
	int garageIndexMap[][];
	int carIndexMap[];
	double minCost = 0;
	double maxDist = 0;
	double maxCost = 0;
	int numNode = 0;
	Integer[] solution;

	
	public static void main(String[] args){
        for ( int i = 0; i < args.length; i+=2 ) {
            if (args[i].equals ("L")) {
                LAMBDA = Double.parseDouble(args[i+1]);
            }
            if (args[i].equals ("C")) {
                CAPACITY = Integer.parseInt(args[i+1]);
            }
        }
		System.out.println("Simulation stats");
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		System.out.println(timeStamp); 
		
		for(int p = 0; p < 100; p++ ) {
			Random rand = new Random();
			total_capacity = 0;
			int caps[] = new int[4];
			caps[0] = rand.nextInt(CAPACITY);
			total_capacity += caps[0];
			if (CAPACITY-total_capacity > 0) {
				caps[1] = rand.nextInt(CAPACITY-total_capacity);				
			} else {
				caps[1] = 0;
			}
			total_capacity += caps[1];
			if (CAPACITY-total_capacity > 0) {
				caps[2] = rand.nextInt(CAPACITY-total_capacity);				
			} else {
				caps[2] = 0;
			}
			total_capacity += caps[2];
			caps[3] = CAPACITY-total_capacity;
			total_capacity = CAPACITY;
			
			for (int n = 0; n < 100; n++) {
			BTree marketStreeParkingManager = new BTree();
        	
			marketStreeParkingManager.garage_capacity.add(caps[0]);
			Garage bush350 = new Garage("g1", 0, 0, 25, caps[0]);
	
			marketStreeParkingManager.garage_capacity.add(caps[1]);
			Garage california555 = new Garage("g2", 0, 1056, 16, caps[1]);
			
			marketStreeParkingManager.garage_capacity.add(caps[2]);
			Garage bush225 = new Garage("g3", -528, 0, 13, caps[2]);
			
			marketStreeParkingManager.garage_capacity.add(caps[3]);			
			Garage halleck240 = new Garage("g4", 492, 712, 10, caps[3]);
			
			marketStreeParkingManager.garages.add(halleck240);
			marketStreeParkingManager.garages.add(bush225);
			marketStreeParkingManager.garages.add(california555);		
			marketStreeParkingManager.garages.add(bush350);
			
	
			marketStreeParkingManager.manage_traffic();
			marketStreeParkingManager = null;
			}
		}
		
		System.out.println("Simulation ends");
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println("L = "+LAMBDA);
        System.out.println("C = "+CAPACITY);
            
        double avg_cost_diff=total_cost_diff/num_samples;
        System.out.print("Avg cost diff = " + avg_cost_diff);
        double avg_distance_diff=total_distance_diff/num_samples;
        System.out.println(" Avg dist diff = " + avg_distance_diff);

        System.out.println(timeStamp);
            
	}
	
	public void manage_traffic(){
		
	//	try{
			for(int r = 0; r < 10; r++) {
//				System.out.println();
//	        	if(pending_vehicles.size() > 0) {
//		        	pending_vehicles.clear();  
//		        	System.gc();
//	        	}	
				
	        	pending_vehicles.clear();  
	        	clean_node(root);
	        	root = null;
	        	System.gc();
//	        	try {
//	        		TimeUnit.SECONDS.sleep(2);
//	        	} catch (InterruptedException e) {
//	        		System.out.println("sleep failed");
//	        	}
	        	
	        	minCost = 0;
	        	maxDist = 0;
	        	maxCost = 0;
	        	numNode = 0;
				
				numCars = getPoisson(LAMBDA);
				while(numCars == 0 || (CAPACITY > 5 && numCars > CAPACITY*1.2)){
					numCars = getPoisson(LAMBDA);
				}
							
				nonEmptyGarages = 0;
                for (int j = 0; j < numGarages; j++) {
                	if (garage_capacity.get(j) > 0) nonEmptyGarages++;
                }
                
				costMatrix = new double[numCars][numGarages];
				solution = new Integer[numCars];
				double costMatrixSorted[][] = new double[numCars][nonEmptyGarages];
				garageIndexMap = new int[numCars][nonEmptyGarages];
				double[][] feeMatrix = new double[numCars][numGarages];
				double[][] distMatrix = new double[numCars][numGarages];
								
				if(numCars > 0) {
			        for(int i = 0; i < numCars; i++){
						int duration = (int)(Math.random()*8+1);
			    		Car car = new Car((int)(Math.random()*3000-1500), (int)(Math.random()*3000-1500), goal, duration);
			    		pending_vehicles.add(car);
		
			        }    
		        }
				
		        
				for(int i = 0; i < numCars; i++){
					for(int j = 0; j < numGarages; j++) {
						double cost = pending_vehicles.get(i).getParkingDuration() * garages.get(j).getParkingRate();
			        	double dist = Math.sqrt(Math.pow(pending_vehicles.get(i).getLocation_x() - garages.get(j).getLocation_x(),2) +
				        				Math.pow(pending_vehicles.get(i).getLocation_y() - garages.get(j).getLocation_y(), 2));
			        	distMatrix[i][j] = dist;
			        	feeMatrix[i][j] = cost;

			        	if(maxDist < dist)
					    	maxDist = dist;
					    
					    if(maxCost < cost)
					    	maxCost = cost;
					    
					}
				}
				
//				System.out.println("maxCost: " +maxCost);
				
				for(int i = 0; i < numCars; i++){
					for(int j = 0; j < numGarages; j++) {
						/*double cost = pending_vehicles.get(i).getParkingDuration() * garages.get(j).getParkingRate();
			        	double dist = Math.sqrt(Math.pow(pending_vehicles.get(i).getLocation_x() - garages.get(j).getLocation_x(),2) +
				        				Math.pow(pending_vehicles.get(i).getLocation_y() - garages.get(j).getLocation_y(), 2));*/
						
					    costMatrix[i][j] = alpha * Math.pow(feeMatrix[i][j]/maxCost, eta) + beta * Math.pow(distMatrix[i][j]/maxDist, zeta);
					  
					    if(minCost < costMatrix[i][j])
					    	minCost = costMatrix[i][j];
					}
				}
				
				//Pre-processing (eliminate 0 spot garage and sorting)
				int index;
				for (int i = 0; i < numCars; i++) {
					double temp[] = new double[numGarages];
					for (int j = 0; j < numGarages; j++) temp[j] = costMatrix[i][j];
					Arrays.sort (temp);
					int k = 0;
					boolean[] flag = new boolean [numGarages];
					for (int j= 0; j < numGarages; j++) flag[j] = false;
					for (int j = 0; j < numGarages; j++) {
						index = 0;
						while (temp[j] != costMatrix[i][index] || flag[index]) 
							index++;
						if (garage_capacity.get(index) > 0) {
							costMatrixSorted[i][k] = costMatrix[i][index];
							garageIndexMap[i][k] = index;
							k++;
						}
						flag[index] = true;
					}
				}
				double temp[] = new double[numCars];
				for (int i = 0; i < numCars; i++) temp[i] = costMatrixSorted[i][0];
				Arrays.sort (temp);
				carIndexMap = new int [numCars];
				boolean[] flag = new boolean [numCars]; 
				for (int i = 0; i < numCars; i++) flag[i] = false;
				for (int i = 0; i < numCars; i++) {
					index = 0;
					while (temp[i] != costMatrixSorted[index][0] || flag[index]) {
						index++;
					}
					carIndexMap[i] = index;
					flag[index] = true;
				}
				
  /*              String garageCap = String.format ("%s%d_%d_%s", "g_cap", CAPACITY, CAPACITY_VAR, ".csv");
                BufferedWriter g_caps = new BufferedWriter(new FileWriter(garageCap, true));//<-- change this
                String carCost = String.format ("%s%d_%d_%s", "cars_cost", CAPACITY, CAPACITY_VAR, ".csv");
                BufferedWriter cars_cost = new BufferedWriter(new FileWriter(carCost, true));//<-- change this
                String carsBreakdown = String.format ("%s%d_%d_%s", "cars_breakdown", CAPACITY, CAPACITY_VAR, ".csv");
                BufferedWriter car_min = new BufferedWriter(new FileWriter(carsBreakdown, true));//<-- change this
	    		StringBuilder g_caps_sb = new StringBuilder();
	    	    StringBuilder cars_cost_sb = new StringBuilder();
	    	    StringBuilder car_min_sb = new StringBuilder();
                
			
	    	    if (r==0) {
                g_caps_sb.append("g1");
	        	g_caps_sb.append(",");
	        	g_caps_sb.append("g2");
	        	g_caps_sb.append(",");	        
	        	g_caps_sb.append("g3");
	        	g_caps_sb.append(",");
	        	g_caps_sb.append("g4");
	        	g_caps_sb.append("\n");
                }
		    
			    for(int cap: garage_capacity){
			    		g_caps_sb.append(cap);
			    		g_caps_sb.append(",");
			    }
			    
                g_caps_sb.setLength(g_caps_sb.length() - 1);
				g_caps_sb.append("\n");
		
			    g_caps.write(g_caps_sb.toString());
			    
                g_caps.close();*/
			    
				minCost = minCost * numCars * numGarages * 1000;
				root = new Node();
				root.carIndex = 0;
				root.garageIndex = 0; 
				root.totalCost = 0.0;
				root.parkingFlag = new boolean[numCars][numGarages];
				for(int i = 0; i < numCars; i++)
					for(int j = 0; j < numGarages; j++)
						root.parkingFlag[i][j] = false;
				
				
                
				buildTree(root);
				
                
      /*          if (r==0) {
		        car_min_sb.append("Car #");
		        car_min_sb.append(",");
		        car_min_sb.append("min cost");
		        car_min_sb.append(",");
		        car_min_sb.append("min dis");
		        car_min_sb.append(",");
		        car_min_sb.append("assigned cost");
		        car_min_sb.append(",");
		        car_min_sb.append("assigned distance");
		        car_min_sb.append(",");
		        car_min_sb.append("n.cost");
		        car_min_sb.append(",");
		        car_min_sb.append("n.distance");
		        car_min_sb.append("\n");
                }*/
                
                
		   /*     for(int i = 0; i < numCars; i++){
	        		for(int j = 0; j < numGarages; j++){
	        			distMatrix[i][j] = (int) (Math.sqrt(Math.pow(pending_vehicles.get(i).getLocation_x() - garages.get(j).getLocation_x(),2) +
		        				Math.pow(pending_vehicles.get(i).getLocation_y() - garages.get(j).getLocation_y(), 2)));
	        		}
		        }
		        
		        for(int i = 0; i < numCars; i++){
        			for(int j = 0; j < numGarages; j++){
        			feeMatrix[i][j] = (int)(pending_vehicles.get(i).getParkingDuration() * garages.get(j).getParkingRate());
        			}
		        }*/
		        
		        for(int i = 0; i < numCars; i++){
	        		double car_min_cost = feeMatrix[i][0];
	        		double car_min_distance = distMatrix[i][0];
	        		
        			for(int j = 0; j < numGarages; j++){
	        			if(car_min_cost > feeMatrix[i][j])
	        				car_min_cost = feeMatrix[i][j];
	        			if(car_min_distance > distMatrix[i][j])
	        				car_min_distance = distMatrix[i][j];
        			}
        			
        		/*	car_min_sb.append(i);
        			car_min_sb.append(",");
        			car_min_sb.append(car_min_cost);
        			car_min_sb.append(",");
        			car_min_sb.append(car_min_distance);
        			car_min_sb.append(",");*/
        			int assigned_g_index = solution[i];
  /*      			car_min_sb.append(feeMatrix[i][assigned_g_index]);
        			car_min_sb.append(",");
        			car_min_sb.append(distMatrix[i][assigned_g_index]);
        			car_min_sb.append(",");
        			car_min_sb.append(feeMatrix[i][assigned_g_index]/maxCost);
        			car_min_sb.append(",");
        			car_min_sb.append(distMatrix[i][assigned_g_index]/maxDist);
        			car_min_sb.append("\n");*/
                    
        			if (assigned_g_index >= 0) {
        				total_cost_diff = total_cost_diff + feeMatrix[i][assigned_g_index] - car_min_cost;
        				total_distance_diff = total_distance_diff + distMatrix[i][assigned_g_index] - car_min_distance;
        				num_samples ++;
        			}
	        			
		        }
		       /* car_min.write(car_min_sb.toString());
		        car_min.close();*/
				
                
                
           /*     if (r==0) {
				cars_cost_sb.append("LAMBDA");
		        cars_cost_sb.append(",");
		        cars_cost_sb.append("#cars");
		        cars_cost_sb.append(",");
		        cars_cost_sb.append("Total Cost");
		        cars_cost_sb.append("\n");
                }
                
		        cars_cost_sb.append(LAMBDA);
		        cars_cost_sb.append(",");
		        cars_cost_sb.append(numCars);
		        cars_cost_sb.append(",");
		        cars_cost_sb.append(minCost);	
		        cars_cost_sb.append("\n");*/
		        iterations++;
		        if (iterations%10000 == 0) {
                    System.out.print("Total cars = " + num_samples);
                    double avg_cost_diff=total_cost_diff/num_samples;
                    System.out.print(" Avg cost diff = " + avg_cost_diff);
                    double avg_distance_diff=total_distance_diff/num_samples;
                    System.out.println(" Avg dist diff = " + avg_distance_diff);
                }
		        root = null;
		     /*   cars_cost.write(cars_cost_sb.toString());
		        cars_cost.close();*/
		        
		        
			}
			
	//	}
	//	catch (IOException e) {
	 //   		System.out.println("file write failed"); 
	//	}
	}
	
	public void clean_node(Node root) {
		if(root != null) {
			if(root.left != null) {
				clean_node(root.left);
				root.left = null;
			}
			if(root.right != null) {
				clean_node(root.right);
				root.right = null;
			}
			root = null;
		}
	}
	
	public void buildTree(Node root){	  
		  double leftCost = root.totalCost; //car root.carIndex does not park at root.garageIndex
		  int garageIndex = root.garageIndex;
		  int carIndex  = root.carIndex;
		  //parkedFlag = root->GetParkedFlag()

		  if (garageIndex == nonEmptyGarages) {
		    carIndex++;
		    if (carIndex == numCars) { // All car/garage combinations are searched, tree building, a.k.a, search is done, we should have the solution at this point
		      return;
		    }
		    if (numNode > 8000000) {
		    	return;
		    }
		    garageIndex = 0;
		  }
		  
		  int newGarageIndex = garageIndex + 1;
		  int newCarIndex = carIndex;
		  garageIndex = garageIndexMap[carIndexMap[carIndex]][garageIndex];

		  // Determine if to trim the left branch
		  boolean parked = true;
		  
		  int occupiedSpots = 0;
		  for (int m = 0; m < numCars; m++) {
	            for (int n = 0; n < numGarages; n++) {
	                 if (root.parkingFlag[m][n]) {
	                      occupiedSpots++;
	                      break;
	                  }
	            }
		  }
		  
		  if (numCars-newCarIndex <= total_capacity-occupiedSpots && newGarageIndex == nonEmptyGarages) {
			  parked = root.parked(carIndex);
		  }
		  
		/*  if (leftTruncation && newGarageIndex == numGarages) { //last garage to park, if we have not packed root->carIndex yet, we need to park now
		    parked = root.parked(carIndex);
		  }
		  
		  if (newCarIndex == numCars) { // This is the last car and it is already parked
		    parked = !root.parked(carIndex);
		  }*/
		  

		  if (parked) { //create left child, i.e., we do not park root->carIndex in root->garageIndex
//			  System.out.println("build left");
//			  System.out.println("Car: " + carIndex +" Garage: " + garageIndex);

			  root.left = new Node();
			  root.left.totalCost = leftCost;
			  root.left.carIndex = newCarIndex;
			  root.left.garageIndex = newGarageIndex;
			  root.left.parkingFlag = root.parkingFlag;
			  
			  numNode++;
			  
//			  for(int i = 0; i < root.parkingFlag.length; i++)
//				  for(int j = 0; j < root.parkingFlag[i].length; j++)
//					  root.left.parkingFlag[i][j] = root.parkingFlag[i][j];
			  
		  }
		  
		  double rightCost = root.totalCost + costMatrix[carIndexMap[carIndex]][garageIndex]; //car root.carIndex parks at root.garageIndex

		  // Determine if to trim right branch
		  parked = root.parked (carIndex);
		  occupiedSpots = 0;
		  for (int m = 0; m < numCars; m++) {
			    // check if car m already parked at root->garageIndex
			    if (root.parkingFlag[m][garageIndex])
			    	occupiedSpots++;
		  }
		  if (!parked // root->carIndex has not been assigned a parking yet
		      && occupiedSpots < garage_capacity.get(garageIndex) // still spots available at root->garageIndex
		      && rightCost < minCost) { //cost is still competitive
		    //Create right child, i.e., we park root->carIndex in root->garageIndex
//			  System.out.println("build right");
//			  System.out.println("Car: " + carIndex +" Garage: " + garageIndex);

			  numNode++;
			  root.right = new Node();
			  root.right.totalCost = rightCost;
			  root.right.carIndex = newCarIndex;
			  root.right.garageIndex = newGarageIndex;
			  root.right.parkingFlag = new boolean[numCars][numGarages];
			  for(int i = 0; i < root.parkingFlag.length; i++)
				  for(int j = 0; j < root.parkingFlag[i].length; j++)
					  root.right.parkingFlag[i][j] = root.parkingFlag[i][j];

		    root.right.parkingFlag[carIndex][garageIndex] = true;
		    
		    // Check if all the cars are parked at this point, i.e., we reached a viable solution
            boolean isLeaf = false;
            if (carIndex == numCars-1) isLeaf = true;
            occupiedSpots = 0;
            for (int m = 0; m < numCars; m++) {
              for (int n = 0; n < numGarages; n++) {
                  if (root.right.parkingFlag[m][n]) {
                      occupiedSpots++;
                      break;
                  }
              }
            }
            if (occupiedSpots == total_capacity) isLeaf = true;
		    if (isLeaf) { //leaf node
		      if (rightCost < minCost) {//is this the best solution so far?
		        minCost = rightCost;
		        //Output parking assignment
		        for (int m = 0; m < numCars; m++) {
		        	solution[carIndexMap[m]] = -1;
		        	for (int n = 0; n < numGarages; n++) {
		            if (root.right.parkingFlag[m][n]) { //car m parks at garage n
		              solution[carIndexMap[m]] = n;
		              break;
		            }
		          }
		        }
		      }

		    }
		  }
		  
		  if (root.right != null) {
		    buildTree (root.right);
		  }
		  
		  if (root.left != null) {
		    buildTree (root.left);
		  }
	}


	
	public int getPoisson(double lambda){
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		
		do {
			k++;
			p *= Math.random();
		} while(p > L);
		
		return k-1;
	}
	
	class Node {
		private int carIndex;
		private int garageIndex;
		private double totalCost;
		private boolean parkingFlag[][];
        
		Node left;
		Node right;
		
		public Node(){
			this.totalCost = 0.0;
			this.left = null;
			this.right = null;	
			
//			for(int i = 0; i < numCars; i++)
//				for(int j = 0; j < numGarages; j++)
//					parkingFlag[i][j] = false;
		}
		
		public boolean parked(int carIndex){
			  boolean parked = false; //Car root->CarIndex is not parked in any garage
			  for (int m = 0; m < numGarages; m++) {
			    if (parkingFlag[carIndex][m]) {
			      parked = true; //Car root->carIndex is parked at garage m
			      break;
			    }
			  }
			  return parked;
		}
		
	}
}


