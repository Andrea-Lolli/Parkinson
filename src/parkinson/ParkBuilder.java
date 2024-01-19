package parkinson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactory;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class ParkBuilder implements ContextBuilder<Object> {
	
	// Parametri simulazione
	// Dimensioni ambiente
	private int dimX = 100;
	private int dimY = 100;
	//Numero agenti
	private int neuronCcount = 240;
	private int neuronDcount = 240;
	private int microgliaCount = 100;
	private int astrociteCount = 100;
	// Distanza agenti
	private int connectionDistance = 13; 
	private int neuronMaxDistance = 3;
	private int agentMaxDistance = 3;
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("Parkinson");
		
		// Crea network
        NetworkFactory netFactory = NetworkFactoryFinder.createNetworkFactory(null);
        Network<Object> network = netFactory.createNetwork("network", context, true);  
        // Crea network morta
        Network<Object> deadNetwork = netFactory.createNetwork("deadNetwork", context, true);
		
		// crea spazio continuo
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), dimX, dimY);
		
		// Crea griglia
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);	
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), false, dimX, dimY));
		for(int i = 0; i < neuronCcount; i++) {
			context.add(new NeuroneMotorio(space, grid, network, deadNetwork));
		}
		
		
		for(int i = 0; i < neuronDcount; i++) {
			NDp neuron = new NDp(space, grid, network, deadNetwork);
			context.add(neuron);
		}

		for(int i = 0; i < astrociteCount; i++) {
			context.add(new Astrocita(space, grid));
		}
		
		
		for(int i = 0; i < microgliaCount; i++) {
			context.add(new Microglia(space, grid));
		}

		
		// Calcolo posizioni ambiente
        double[][] neuronPoints = generatePoints(neuronDcount + neuronCcount, dimX, neuronMaxDistance);
        double[][] otherAgentsPoints = generatePoints(microgliaCount + astrociteCount, dimX, agentMaxDistance);
        
        int neuronCount = 0;
        int agentCount = 0;
        
        // Posiziona agenti
		for(Object obj : context) {
			if(isNeuron(obj)) {
				grid.moveTo(obj, (int) neuronPoints[neuronCount][0], (int) neuronPoints[neuronCount][1]);
				space.moveTo(obj, neuronPoints[neuronCount][0], neuronPoints[neuronCount][1]);
				neuronCount++;
			} else {
				grid.moveTo(obj, (int) otherAgentsPoints[agentCount][0], (int) otherAgentsPoints[agentCount][1]);
				space.moveTo(obj, otherAgentsPoints[agentCount][0], otherAgentsPoints[agentCount][1]);
				agentCount++;
			}
		}
		
		// Genera connessioni fra neuroni
		List<int[]> connections = findConnections(neuronPoints, connectionDistance);
		List<Object> neurons = new ArrayList<>();

		for(Object obj : context) { 
			if(isNeuron(obj) )
				neurons.add(obj);
		}
		for(int[] c : connections) {
				network.addEdge(neurons.get(c[0]), neurons.get(c[1]));
		}
			
		return context;
	}
	
	private boolean isNeuron(Object obj) {
		if(obj instanceof NDp || obj instanceof NeuroneMotorio) 
			return true;
		return false;
	}
	
	// Collega tutti i punti entro la distanza maxDistance
	// Se non ci sono putni si collega ai 2 punti più vicini (anche se più lontani della distanza)
    private static List<int[]> findConnections(double[][] points, double maxDistance) {
        List<int[]> connections = new ArrayList<>();
        int nPoints = points.length;

        for (int i = 0; i < nPoints; i++) {
            List<int[]> withinDistance = new ArrayList<>();
            List<int[]> allConnections = new ArrayList<>();

            for (int j = 0; j < nPoints; j++) {
                if (i != j) {
                    double distance = calculateDistance(points[i][0], points[i][1], points[j][0], points[j][1]);
                    if (distance < maxDistance) {
                        withinDistance.add(new int[]{i, j});
                    }
                    allConnections.add(new int[]{i, j, (int) distance});
                }
            }

            if (withinDistance.size() >= 2) {
                connections.addAll(withinDistance);
            } else {
                // Se non ha 2 connessioni, ordina le connessioni in base alla distanza
                allConnections.sort(Comparator.comparingInt(connection -> connection[2]));

                // aggiunge i due neuroni più vicini
                for (int k = 0; k < Math.min(2, allConnections.size()); k++) {
                    connections.add(new int[]{i, allConnections.get(k)[1]});
                }
            }
        }

        return connections;
    }

    // Distanza euclidea
    private static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
	
    // Generatore di punti randomici
    public static double[][] generatePoints(int nPoints, int gridDimension, int distance) {
        if (nPoints > gridDimension * gridDimension) {
            throw new IllegalArgumentException("Troppi punti non ci stanno nello spazio");
        }

        double[][] points = new double[nPoints][2];
        Random random = new Random();

        for (int i = 0; i < nPoints; i++) {
            int x, y;
            do {
                x = random.nextInt(gridDimension);
                y = random.nextInt(gridDimension);
            } while (!isValidPoint(points, i, x, y, distance));

            points[i][0] = x;
            points[i][1] = y;
        }

        return points;
    }

    private static boolean isValidPoint(double[][] points, int currentIndex, int x, int y, int distance) {
        for (int i = 0; i < currentIndex; i++) {
            double currentDistance = calculateDistance(points[i][0], points[i][1], x, y);
            if (currentDistance < distance) {
                return false;
            }
        }
        return true;
    }
}
