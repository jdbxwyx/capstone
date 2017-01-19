import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class geneticLogic {

	private static int numMachines;
	private static int machineId;
	//public static void main(String[] args) throws IOException, InterruptedException {
	public static void geneticLogic(int num, int id) throws IOException, InterruptedException {
		numMachines = num;
		machineId = id;
		
		//the initial population of size 6
		// to make paralleling work easier, make it size = number of machines * number of cores on each machine
		
		int population = numMachines * 3;
		int[][] initialPopulation = new int[population][2];
		
		boolean maxFitnessFound = false;
		
		
		//populating the initial population
		for(int i = 0 ; i < initialPopulation.length ; i++ ) {
			
			//the first value is the number of topics. Assign a range which you think is reasonable
			//the second value is the number of iterations
			initialPopulation[i][0] = (int) Math.floor(Math.random()*12 + 3);
			initialPopulation[i][1] = (int) Math.floor(Math.random()*1000 + 1);
			
			//initialPopulation[i][0] = 2;
		    //initialPopulation[i][1] = 500;
		}
		
		while( !maxFitnessFound) {
			
			//to get the fitness values
			double[] fitnessValues = new double[population];
		
			/**
			 * the total number of documents that are being processed. Put them in a folder and add the folder path here.
			 */
			int numberOfDocuments = new File("txtData").listFiles().length;
			//create an instance of the topic modelling class
			TopicModelling tm = new TopicModelling();
			
			
			
			long startTime = System.currentTimeMillis();
		
			int coresNum = 4;
			int newThreadsNum = 3;
			Thread threads[] = new Thread[newThreadsNum];
			for(int i = 0; i < newThreadsNum; i++){
				threads[i] = new Thread( new MyThread(i, numMachines, machineId, initialPopulation, tm, numberOfDocuments, fitnessValues));
				threads[i].start(); 
			}
			
			for(int i = 0; i < newThreadsNum; i++){
				threads[i].join();
				System.out.println("Thread " + i + " joined");
			}
			
			long paraEndTime = System.currentTimeMillis();
			System.out.println("parallel part takes " + (paraEndTime - startTime) + "ms");		
		
			//ranking and ordering the chromosomes based on the fitness function. 
			//no sorting code found?(by Xiaolin)
			//We need only the top 1/3rd of the chromosomes with high fitness values - Silhouette coefficient
			int[][] newPopulation = new int[initialPopulation.length][2];
			//copy only the top 1/3rd of the chromosomes to the new population 
			for(int i = 0 ; i < (initialPopulation.length / 3) ; i++) {
				System.out.println("time is" + System.currentTimeMillis());
				double maxFitness = Integer.MIN_VALUE;
				int maxFitnessChromosome = -1;
				for(int j = 0 ; j < initialPopulation.length ; j++) {
					if(fitnessValues[j] > maxFitness) {
						maxFitness = fitnessValues[j];
						
						//stop reproducing or creating new generations if the expected fitness is reached
						/**
						 * Please find what would be a suitable fitness to classify the set of documents that you choose
						 */
						
						// set fitness threshold here!!!
						if(maxFitness > 0.75) {
							//run the function again to get the words in each topic
							//the third parameter states that the topics are to be written to a file
							tm.LDA(initialPopulation[j][0],initialPopulation[j][1], true);
							System.out.println("the best distribution is " + initialPopulation[j][0] + " topics and " + initialPopulation[j][1] + "iterations and fitness is " + maxFitness);
							maxFitnessFound = true;
							break;						
						}
						maxFitnessChromosome = j;
					}
				}
				
				if(maxFitnessFound) {
					break;
				}
			
				//copy the chromosome with high fitness to the next generation
				newPopulation[i] = initialPopulation[maxFitnessChromosome];
				fitnessValues[maxFitnessChromosome] = Integer.MIN_VALUE;
			}
			
			if(maxFitnessFound) {
				break;
			}
		
		
			//perform crossover - to fill the rest of the 2/3rd of the initial Population
			for(int i = 0 ; i < initialPopulation.length / 3  ; i++ ) {
				newPopulation[(i+1)*2][0] = newPopulation[i][0];
				newPopulation[(i+1)*2][1] = (int) Math.floor(Math.random()*1000 + 1);
				newPopulation[(i+1)*2+1][0] = (int) Math.floor(Math.random()*12 + 2);
				newPopulation[(i+1)*2+1][1] = newPopulation[i][1];
			}
		
			//substitute the initial population with the new population and continue 
			initialPopulation = newPopulation;
			
			
			long endTime = System.currentTimeMillis();
			System.out.println("other part takes " + (endTime - paraEndTime) + "ms");
			
			/**The genetic algorithm loop will not exit until the required fitness is reached.
			 * For some cases, we might expect a very high fitness that will never be reached.
			 * In such cases add a variable to check how many times the GA loop is repeated.
			 * Terminate the loop in predetermined number of iterations.
			 */
		}		
		
	}
	
}
	