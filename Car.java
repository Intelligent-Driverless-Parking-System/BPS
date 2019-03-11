
public class Car implements Comparable<Car>{
	static int totalNumberCars = 0;
	
	private int location_x;
	private int location_y;
	private int parkingDuration;
	private int goal;
	private double total_cost;
	private double total_distance;
	private Garage garage;
	private int plateNumber;
	private int parkingTimer;
	
	public Car(int x, int y, int goal, int duration){
		this.location_x = x;
		this.location_y = y;
		this.goal = goal;
		this.parkingDuration = duration;
		this.plateNumber = totalNumberCars++;
		this.parkingTimer = duration * 60; //in minute unit
	}
	
	public int calculateCost(){
		total_cost = garage.getParkingRate() * parkingDuration;
		return (int)total_cost/60;
	}
	
	public double calculateDistance(Garage garage){
		total_distance = Math.sqrt(Math.pow(this.location_x - garage.getLocation_x(),2) +
				Math.pow(this.location_y - garage.getLocation_y(), 2));
		return total_distance;
	}
	
	public int getParkingTimer(){
		return parkingTimer;
	}
	
	public void setParkingTimer(int timer){
		 parkingTimer = timer;
	}
	
	public int getGoal(){
		return goal;
	}
	
	public int getParkingDuration(){
		return parkingDuration;
	}
	
	public int getLocation_x(){
		return location_x;
	}
	
	public int getLocation_y(){
		return location_y;
	}
	

	
	public void setGarage(Garage garage){
		this.garage = garage;
	}
	
	public Garage getGarage(){
		return garage;
	}
	
	public String toString(){
		return Integer.toString(plateNumber);
	}
	
	@Override
	public int compareTo(Car o) {
	  return (int)(o.getParkingDuration() - getParkingDuration());
	}


}
