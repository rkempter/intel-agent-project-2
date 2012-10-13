package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		List<City> cityList = topology.cities();
		for(int i=0; i< cityList.size(); i++){
			cities.add(cityList.get(i));
		}
		List<Vehicle> vehicleList= agent.vehicles();
		ReinforcementLearning rl= new ReinforcementLearning(cities, td, vehicleList.get(0).costPerKm());
		rl.learning();


		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		//		Double discount = agent.readProperty("discount-factor", Double.class,
		//				0.95);
		//
		//		this.random = new Random();
		//		this.pPickup = discount;
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action= null;

		Double pPickup= 0.95;
		Random random= new Random();
		if (availableTask == null || random.nextDouble() > pPickup) {	//no tasks available or choose not to pick up the task
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
}
