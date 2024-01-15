package parkinson;

import java.util.Random;

import parkinson.NDp.StatoInterno;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class NeuroneMotorio {



	private enum StatoInterno {
		consumo, malato
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Object> network;
	private GridPoint pt;

	// descrive lo stato interno.
	private StatoInterno stato;


	private int contaDop;;


	public NeuroneMotorio (ContinuousSpace<Object> space, Grid<Object> grid, Network<Object> network) {
		this.contaDop = 10;
		this.space = space;
		this.grid = grid;
		this.network = network;
		this.stato = StatoInterno.consumo;
	}

	public void bindNeuron(Object obj) {
		this.network.addEdge(this, obj);
	}

	/** 
	 * Consuma 1 unità di dop
	 */
	@ScheduledMethod(start=1, interval=2, priority = 2)
	public void consumaDop() {
		if(stato == StatoInterno.consumo) {
			contaDop--;
		}
	}

	public void addDop() {
		contaDop++;
	}

	@ScheduledMethod(start=1, interval=2, priority = 1)
	public void cambiaStato() {
		if(stato==StatoInterno.consumo) {
			if(contaDop < 3) {
				stato = StatoInterno.malato;
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
}
