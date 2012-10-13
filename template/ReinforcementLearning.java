/*I' ll try to explain everything here. The state is (location, destination) if there is no package is (location, null). 
Just for now costPerKm is the cost of the first vehicle (they are all the same) and DiscountFactor is static. First of all the algorithm builds 
the stateSpace array which contains [[location1,null],[location1,location2].....]. The actions ArrayList is built as follow: For each group of state beginning with the same location 
there is a set of possible actions: p&d and the actions to move to each neighbour city. action.get(0) returns a list of all actions available from the city0.
the function learning executes the offline reinforcement for each state and each action finds the action with the best reward. The value of Qsa is computed as follows:
if p&d: Qsa (i,j)= the reward of the task(i,j)  - the cost to get to that city(i,j) + DiscountFactor* sum( probability having a task from (j, k)(where k is every other city)* reward_state(j,k) )
NB: (in the summation is included also the case where in city j there is no task prob(j, null) returns such probability. The convergence is tested as a simple equality between two arrays (Vs) bc
it converges very fastly and i think we don' t need a stopping criteria. Once the algorithm has converged Vs contains the reward for the best actionfrom that state and Bests contains the best action
(the one which maximizes the reward) to take from that state. Hope everything is clear and sorry if i didn 't send you before but i had lot of things to do this week. Just let me know if everything
is clear and feel free to change anything. Now we just have to implement the simulation part which i think it won' t take much time.
 */

package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logist.task.TaskDistribution;
import logist.topology.Topology.City;

public class ReinforcementLearning  {

	private ArrayList<City> cityList;
	private ArrayList<ArrayList<City>> stateSpace= new ArrayList<ArrayList<City>>();
	private ArrayList<ArrayList<Object>> actions= new ArrayList<ArrayList<Object>>();
	private TaskDistribution taskDist;
	private int costPerKM;


	private static double DiscountFactor= 0.9;

	public ReinforcementLearning(ArrayList<City> cityL, TaskDistribution td, int costPerKM){

		taskDist= td;
		cityList= cityL;
		this.costPerKM= costPerKM;

		for ( int i=0; i< cityList.size(); i++ ){
			for (int j=0; j< cityList.size(); j++){
				if (cityList.get(i)!=cityList.get(j)){
					stateSpace.add(new ArrayList<City>());
					stateSpace.get((i*cityList.size())+j).add(cityList.get(i));
					stateSpace.get((i*cityList.size())+j).add(cityList.get(j));
					//System.out.println("Reward from city "+cityList.get(i)+ " to city "+ cityList.get(j) +" is "+ td.reward(cityList.get(i), cityList.get(j)));
				}
				else{
					stateSpace.add(new ArrayList<City>());
					stateSpace.get((i*cityList.size())+j).add(cityList.get(i));
					stateSpace.get((i*cityList.size())+j).add(null);
				}
				//System.out.println(stateSpace.get((i*cityList.size())+j));
			}

		}
		System.out.println(stateSpace.size());

		for(int i=0; i< cityList.size(); i++){
			actions.add(new ArrayList<Object>());
			actions.get(i).add("p&d");
			List<City> neighbours= cityList.get(i).neighbors();
			//System.out.println(cityList.get(i)+" "+neighbours);
			for(int j=0; j< neighbours.size(); j++){
				actions.get(i).add(neighbours.get(j));
			}
			//System.out.println("city: "+ cityList.get(i) +" actions: "+ actions.get(i));
		}
	}
	public void learning(){
		ArrayList<ArrayList<Double>> Qsa= new ArrayList<ArrayList<Double>>();
		ArrayList<Double> Vs= new ArrayList<Double>(Collections.nCopies(stateSpace.size(), 0.0));
		ArrayList<Double> previousVs= new ArrayList<Double>(Collections.nCopies(stateSpace.size(), 0.0));
		ArrayList<Object> BestS= new ArrayList<Object>(Collections.nCopies(stateSpace.size(), 0.0));

		do{
			for (int i=0; i<Vs.size(); i++){
				previousVs.set(i, Vs.get(i));
			}
			for( int i=0; i< stateSpace.size(); i++){
				Qsa.add(new ArrayList<Double>());
				for(int j=0; j< actions.get(i/cityList.size()).size(); j++){
					//System.out.println("state: "+ stateSpace.get(i)+ " actions: "+ actions.get(i/cityList.size()));
					if(stateSpace.get(i).get(1)!=null && actions.get(i/cityList.size()).get(j).equals("p&d")){
						Qsa.get(i).add(computeCurrentQsa(stateSpace.get(i).get(0), stateSpace.get(i).get(1), Vs, actions.get(i/cityList.size()).get(j))) ;
					}
					else if(actions.get(i/cityList.size()).get(j)!= "p&d" ){
						Qsa.get(i).add(computeCurrentQsa(stateSpace.get(i).get(0), (City)actions.get(i/cityList.size()).get(j), Vs, actions.get(i/cityList.size()).get(j))) ;
					}
					else{
						//it can not happen that i pick up a task if there is no task (setting to -inf).
						Qsa.get(i).add(Double.NEGATIVE_INFINITY);
					}
				}
				Vs.set(i, Collections.max(Qsa.get(i)));										//save reward for the best action from state s
				BestS.set(i, actions.get(i/cityList.size()).get(Qsa.get(i).indexOf(Collections.max(Qsa.get(i)))));		//save best action so far from state s
				//System.out.println("state: "+ stateSpace.get(i)+ " actions: "+ actions.get(i/cityList.size())+ " "+ Vs.get(i)+ " "+ BestS.get(i));
			}
			Qsa.clear();
			//			if(!Vs.equals(previousVs)){
			//				System.out.println("Vs--->"+ Vs);
			//				System.out.println("previousVs--->"+ previousVs);
			//			}

		}while(!checkConvergence(Vs, previousVs));
		for(int i=0; i< BestS.size(); i++){
			System.out.println(stateSpace.get(i)+ " best action is: "+ BestS.get(i));
		}
	}
	public double computeCurrentQsa(City currentCity, City destinationCity, ArrayList<Double> Vs, Object action){
		double currentQsa= 0.0;
		double futureHorizon= 0.0;

		if(action.equals("p&d")){
			currentQsa= taskDist.reward(currentCity, destinationCity) - (currentCity.distanceTo(destinationCity) * costPerKM);
		}
		else{
			currentCity.distanceTo(destinationCity);
			currentQsa= - currentCity.distanceTo(destinationCity) * costPerKM;
		}

		for (int i=0; i< stateSpace.size(); i++){
			//take into account also the case when there is no package (i.e the second field is null) bc probability(city1 , null) returns the probability there is no task in city city1.
			if( stateSpace.get(i).get(0)== destinationCity){
				futureHorizon += taskDist.probability(destinationCity, stateSpace.get(i).get(1)) * Vs.get(i); 
			}
		}
		currentQsa += DiscountFactor * futureHorizon;
		return currentQsa;
	}
	public boolean checkConvergence(ArrayList<Double> Vs, ArrayList<Double> PreviousVs){
		//return true if converged (just compares the two arrayList, if are equal converged =true)
		boolean converged= false;
		if(Vs.equals(PreviousVs)){
			converged= true;
		}
		return converged;

	}
}
