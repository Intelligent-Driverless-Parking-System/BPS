import java.util.ArrayList;

public class Garage implements Comparable<Garage>{
	private String name;
	private int location_x;
	private int location_y;
	private double parkingRate;
	private int capacity;
	private int occupancy;
	ArrayList<Car> vehicles = new ArrayList<Car>();
	
	public Garage(String name, int x, int y, int rate, int capacity){
		this.name = name;
		this.location_x = x;
		this.location_y = y;
		this.parkingRate = rate;
		this.capacity = capacity;
		this.occupancy = 0;
		
	}
	
	public void clear_occupant(){
		vehicles.clear();
		occupancy = 0;
	}
	
	public int getLocation_x(){
		return location_x;
	}
	
	public int getLocation_y(){
		return location_y;
	}
	
	public double getParkingRate(){
		return parkingRate;
		
	}
	
	public int getCapacity(){
		return capacity;
	}

	public boolean spot_available(){
		return (capacity > occupancy);
	}
	
	public int getOccupancy(){
		return occupancy;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean reserve(Car car){
		if(spot_available()){
			vehicles.add(car);
			occupancy++;
			car.setGarage(this);
			double cost = car.calculateCost();
//			System.out.println(car + " parked at " + this + " with total parking cost " + Double.toString(cost));
			return true;
		} else {
			return false;
		}
	}
	
	public void depart(Car car){
		vehicles.remove(car);
		occupancy--;
		System.out.println(car + " departed from " + this);
	}
	
	public String toString(){
		return (name);
	}

	 @Override
	 public int compareTo(Garage o) {
	   return (int)(getParkingRate() - o.getParkingRate());
	 }

}
