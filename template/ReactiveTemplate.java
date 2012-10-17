package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {
	
	private static double DiscountFactor= 0.99;

	private ArrayList<City> cities= new ArrayList<City>();
	private ArrayList<ArrayList<Object>> BestS = new ArrayList<ArrayList<Object>>(); 			//holds behaviour for multiple vehicles
	private ArrayList<ArrayList<City>> stateSpace = new ArrayList<ArrayList<City>>();

	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		Scanner input= new Scanner(System.in);
		if(DiscountFactor>= 1.0 || DiscountFactor<= 0.0){
			System.out.println("The discount factor must be in a range (0, 1): ");
			DiscountFactor= input.nextDouble();
		}
		
		List<City> cityList = topology.cities();
		for(int i=0; i< cityList.size(); i++){
			cities.add(cityList.get(i));
		}
		
		ReinforcementLearning rl= new ReinforcementLearning(cities, td, DiscountFactor);
		List<Vehicle> vehicleList= agent.vehicles();
		for( int i=0; i< vehicleList.size(); i++){
			BestS.add(new ArrayList<Object>());
			BestS.set(i, rl.learning(vehicleList.get(i)));
		}
		stateSpace = rl.getStateSpace();
	}

	public Action act(Vehicle vehicle, Task availableTask) {
		Action action= null;
		City location = vehicle.getCurrentCity(), destination;

		if(availableTask != null) {
			destination = availableTask.deliveryCity;
		} else {
			destination = null;
		}

		ArrayList<City> state = new ArrayList<City>();
		state.add(location);
		state.add(destination);
		int currentStateIndex = stateSpace.indexOf(state);
		Object bestAction = BestS.get(vehicle.id()).get(currentStateIndex);

		// Check state and get best action
		System.out.println("********************* "+ vehicle.name() +" *********************");
		if(bestAction == "p&d") {
			System.out.println( vehicle.name() +" picks up a new task in "+ state.get(0)+" to be delivered to " + state.get(1));
			action = new Pickup(availableTask);
		} else {
			destination = (City) bestAction;
			System.out.println( vehicle.name() +" moves to "+ destination.name);
			action = new Move(destination);
		}

		return action;
	}
}
