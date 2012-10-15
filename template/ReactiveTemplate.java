package template;

import java.util.ArrayList;
import java.util.List;
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

	private ArrayList<City> cities= new ArrayList<City>();
	private ArrayList<ArrayList<Object>> BestS = new ArrayList<ArrayList<Object>>(); 			//holds behaviour for multiple vehicles
	private ArrayList<ArrayList<City>> stateSpace = new ArrayList<ArrayList<City>>();

	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		List<City> cityList = topology.cities();
		for(int i=0; i< cityList.size(); i++){
			cities.add(cityList.get(i));
		}
		
		ReinforcementLearning rl= new ReinforcementLearning(cities, td);
		List<Vehicle> vehicleList= agent.vehicles();
		for( int i=0; i< vehicleList.size(); i++){
			BestS.add(new ArrayList<Object>());
			BestS.set(i, rl.learning(vehicleList.get(i)));
		}
		stateSpace = rl.getStateSpace();
	}

	public Action act(Vehicle vehicle, Task availableTask) {
		System.out.println("Vehicle id is: "+ vehicle.id());
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
		//System.out.println("This is the state: "+ state);
		int currentStateIndex = stateSpace.indexOf(state);
		Object bestAction = BestS.get(vehicle.id()).get(currentStateIndex);
		System.out.println("Best action is: " + bestAction);

		// Check state and get best action

		if(bestAction == "p&d") {
			System.out.println("Vehicule picks up a new task");
			action = new Pickup(availableTask);
		} else {
			destination = (City) bestAction;
			System.out.println("Vehicule moves to city: "+ destination.name);
			action = new Move(destination);
		}

		return action;
	}
}
