package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;

public class NeuroneMotorio {

	// Parametri simulazione
	private int nAlfaMalato = 3;

	private enum StatoInterno {
		consumo, malato
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Object> network;
	private Network<Object> deadNetwork;

	// descrive lo stato interno.
	private StatoInterno stato;

	private int contaDop;
	private boolean deficit;

	public NeuroneMotorio (ContinuousSpace<Object> space, Grid<Object> grid, Network<Object> network, Network<Object> deadNetwork) {
		this.contaDop = 10;
		this.deficit = true;
		this.space = space;
		this.grid = grid;
		this.network = network;
		this.stato = StatoInterno.consumo;
		this.deadNetwork = deadNetwork;
	}

	public void bindNeuron(Object obj) {
		this.network.addEdge(this, obj);
	}

	/** 
	 * Consuma 1 unità di dop
	 */
	@ScheduledMethod(start=1, interval=2, priority = 2)
	public void consumaDop() {
		if(stato != StatoInterno.malato) {
			contaDop--;
		}
	}

	public void addDop() {
		deficit = false;
		contaDop++;
	}

	@ScheduledMethod(start=1, interval=2, priority = 1)
	public void cambiaStato() {
		if(stato==StatoInterno.consumo) {
			if(contaDop < nAlfaMalato) {
				stato = StatoInterno.malato;
				this.updateNetwork();
			}
		}
	}

	public float getColorR() {
		if(this.stato == StatoInterno.consumo)
			return 255;
		if(this.stato == StatoInterno.malato)
			return 139;
		return 0;
	}

	public float getColorG() {
		if(this.stato == StatoInterno.consumo)
			return 255;
		if(this.stato == StatoInterno.malato)
			return 69;
		return 0;
	}

	public float getColorB() {
		if(this.stato == StatoInterno.consumo)
			return 102;
		if(this.stato == StatoInterno.malato)
			return 19;
		return 0;
	}
	
	private void updateNetwork() {
		Iterable<RepastEdge<Object>> edges = this.network.getEdges(this);
		for (RepastEdge<Object> edge : edges) {
			// aggiunge dopamina
			deadNetwork.addEdge(edge);
			network.removeEdge(edge);
		}
	}
}
