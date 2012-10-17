package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology.City;

public class ReinforcementLearning  {

	private ArrayList<City> cityList;
	private ArrayList<ArrayList<City>> stateSpace= new ArrayList<ArrayList<City>>();
	private ArrayList<ArrayList<Object>> actions= new ArrayList<ArrayList<Object>>();
	private TaskDistribution taskDist;

	private static double DiscountFactor = 0.0;

	public ReinforcementLearning(ArrayList<City> cityL, TaskDistribution td, double dF){
		DiscountFactor= dF;
		taskDist = td;
		cityList = cityL;

		// Create the state space
		for ( int i=0; i< cityList.size(); i++ ){
			for (int j=0; j< cityList.size(); j++){
				if (cityList.get(i) != cityList.get(j)){
					stateSpace.add(new ArrayList<City>());
					stateSpace.get((i*cityList.size())+j).add(cityList.get(i));
					stateSpace.get((i*cityList.size())+j).add(cityList.get(j));
				}
				else{
					stateSpace.add(new ArrayList<City>());
					stateSpace.get((i*cityList.size())+j).add(cityList.get(i));
					stateSpace.get((i*cityList.size())+j).add(null);
				}
			}
		}
		// Create actions for cities
		for(int i=0; i< cityList.size(); i++){
			actions.add(new ArrayList<Object>());
			actions.get(i).add("p&d");
			List<City> neighbours= cityList.get(i).neighbors();
			for(int j=0; j< neighbours.size(); j++){
				actions.get(i).add(neighbours.get(j));
			}
		}
	}
	
	public ArrayList<Object> learning(Vehicle v){	
		// Multiple Q(s), depending on the action -> Best one is stored in V(s) at the end of a loop
		ArrayList<ArrayList<Double>> Qsa = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> Vs = new ArrayList<Double>(Collections.nCopies(stateSpace.size(), 0.0));
		
		// Serves as a converge-control
		ArrayList<Double> previousVs = new ArrayList<Double>(Collections.nCopies(stateSpace.size(), 0.0));
		
		// Best action for state is saved in Best(S)
		ArrayList<Object> BestS = new ArrayList<Object>(Collections.nCopies(stateSpace.size(), 0.0));
		int x = 0;
		do{
			for (int i=0; i<Vs.size(); i++) {
				previousVs.set(i, Vs.get(i));
			}
			for( int i=0; i< stateSpace.size(); i++){
				Qsa.add(new ArrayList<Double>());
				
				// We do i / cityList.size() to get each city (size of stateSpace = cityList x cityList)
				for(int j=0; j < actions.get(i/cityList.size()).size(); j++){
					
					// Only states with package and actions with actions pickup & deliver
					if(stateSpace.get(i).get(1) != null && actions.get(i/cityList.size()).get(j).equals("p&d")) {
						// Calculate QSA with action 'p&d'
						Qsa.get(i).add(computeCurrentQsa(stateSpace.get(i).get(0), stateSpace.get(i).get(1), Vs, actions.get(i/cityList.size()).get(j), v)) ;
					}
					else if(actions.get(i/cityList.size()).get(j)!= "p&d" ){
						// Calculate QSA with action move
						Qsa.get(i).add(computeCurrentQsa(stateSpace.get(i).get(0), (City)actions.get(i/cityList.size()).get(j), Vs, actions.get(i/cityList.size()).get(j), v)) ;
					}
					else{
						//it can not happen that i pick up a task if there is no task (setting to -inf).
						Qsa.get(i).add(Double.NEGATIVE_INFINITY);
					}
				}
				Vs.set(i, Collections.max(Qsa.get(i)));	//save reward for the best action from state s
				//save best action so far from state s
				BestS.set(i, actions.get(i/cityList.size()).get(Qsa.get(i).indexOf(Collections.max(Qsa.get(i)))));
			}
			Qsa.clear();
			x = x+1;
		} while(!checkConvergence(Vs, previousVs));
		
		System.out.println("Number of iterations until convergence: "+ x);
		return BestS;
	}
	
	public double computeCurrentQsa(City currentCity, City destinationCity, ArrayList<Double> Vs, Object action, Vehicle vehicle){
		double currentQsa = 0.0;
		double futureHorizon = 0.0;
		
		if(action.equals("p&d")){
			// Expected reward - cost per kilometer 
			currentQsa =  (taskDist.probability(currentCity, destinationCity) * taskDist.reward(currentCity, destinationCity)) - (currentCity.distanceTo(destinationCity) * vehicle.costPerKm());
		}
		else{
			currentCity.distanceTo(destinationCity);
			currentQsa= - currentCity.distanceTo(destinationCity) * vehicle.costPerKm();
		}

		for (int i=0; i< stateSpace.size(); i++){
			// From state j to all other states
			if( stateSpace.get(i).get(0) == destinationCity){
				futureHorizon +=  taskDist.probability(destinationCity, stateSpace.get(i).get(1)) * Vs.get(i); 
			}
		}
		currentQsa += DiscountFactor * futureHorizon;
		
		return currentQsa;
	}
	
	public boolean checkConvergence(ArrayList<Double> Vs, ArrayList<Double> previousVs){
		//return true if converged (just compares the two arrayList, if are equal converged =true)
		boolean converged = false;
		
		double eps = Math.pow(2, -40);
		double maxValue = 0;
		for(int i = 0; i < Vs.size(); i++) {
			double currentVal = Math.abs(Vs.get(i)-previousVs.get(i));
			if(currentVal > maxValue) {
				maxValue = currentVal;
			}
		}
		if(maxValue <= (2*eps*DiscountFactor)/(1-DiscountFactor)) {
			converged = true;
		}
		
		return converged;
	}
	
	public ArrayList<ArrayList<City>>getStateSpace() {
		return stateSpace;
	}
}
