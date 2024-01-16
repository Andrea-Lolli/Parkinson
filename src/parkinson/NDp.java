package parkinson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import repast.simphony.context.Context;
import repast.simphony.context.Contexts;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.query.space.grid.GridCell;

public class NDp {
	
	// Parametri simulazione neurone
	private int nAlfaAccumulo = 4;
	private int nAlfaMalato = 12;
	private int probAccumuloAlfa = 5; // (1/probAccumuloAlfa) = probabilità di incrementare alfasinucleina interna
	private int probRilascio = 5; // (1/proRilascio) = probabilità di generare agenti in ognuna delle direzioni
	
	public enum StatoInterno{
		produzione, accumulo, malato;
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Object> network;
	private Network<Object> deadNetwork;

	// descrive lo stato interno.
	private StatoInterno stato;

	//private List<Alfa> buffer;
	private int contaAlfa;

	// contro le citochine
	private int resistenza;

	public NDp (ContinuousSpace<Object> space, Grid<Object> grid, Network<Object> network, Network<Object> deadNetwork) {
		this.space = space;
		this.grid = grid;
		this.stato = StatoInterno.produzione;
		this.contaAlfa = 0;
		this.network = network;
		this.stato = StatoInterno.produzione;
		this.deadNetwork = deadNetwork;
	}

	public float getColorR() {
		if(stato == StatoInterno.accumulo) 
			return 160;
		if(stato == StatoInterno.malato) 
			return 70;
		if(stato == StatoInterno.produzione)
			return 144;
		return 0;
	}

	public float getColorG() {
		if(stato == StatoInterno.accumulo)
			return 32;
		if(stato == StatoInterno.malato)
			return 70;
		if(stato == StatoInterno.produzione)
			return 238;
		return 0;
	}

	public float getColorB() {
		if(stato == StatoInterno.accumulo)
			return 240;
		if(stato == StatoInterno.malato)
			return 70;
		if(stato == StatoInterno.produzione)
			return 144;
		return 0;
	}

	/**
	 * Metodo produzione Dopamina.
	 * Passaggio di dopamina sull'arco di collegamento.
	 * Ogni tick dell'ambiente produce dopamina.
	 * Ogni tick c'è la probabilità di creare alfa-sin.
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public void dopRelease() {			
		//flag controllo stato
		if(this.stato == StatoInterno.produzione || this.stato==StatoInterno.accumulo) {

			Iterable<Object> items = this.network.getAdjacent(this);

			for (Object neighbor : items) {
				// aggiunge dopamina
				if(neighbor instanceof NeuroneMotorio) {
					((NeuroneMotorio)neighbor).addDop();
				}
			}
		}
	}

	/**
	 * accumulo di alfa-sinucleina malformata.
	 * Dopo 3 elementi di Alfa.java lo stato cambia in accumulo.
	 */
	@ScheduledMethod(start = 1, interval = 2, priority = 1)
	public void accumulo() {
		if(stato != StatoInterno.malato && new Random().nextInt(probAccumuloAlfa)< 1) {
			this.contaAlfa ++;
			if(this.contaAlfa >= nAlfaAccumulo) {
				this.stato = StatoInterno.accumulo;
			}
		}		
	}

	/**
	 * Ogni tre tick vengono rilasciate in 8 direzioni diverse 8 molecole di segnale
	 */
	@ScheduledMethod(start = 1, interval = 3)
	public void stressRelease () {
		if(stato==StatoInterno.accumulo) {
			GridPoint pt = grid.getLocation(this);
			for (int i = 0; i < 8; i++) {
				if(new Random().nextInt(probRilascio) < 1 && pt != null) {
					try {
						StressSign ss = new StressSign(space, grid, pt, (double)i*45);
						ContextUtils.getContext(this).add(ss);
						ss.build();
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}	
				}
			}
		}
	}

	/**
	 * Rilascio alfa e passo allo stato malato.
	 */
	@ScheduledMethod(start=1,interval = 1)
	public void alfaRelease() {
		if(this.contaAlfa>=12 && stato==StatoInterno.accumulo) {	
			GridPoint pt = grid.getLocation(this);
			for (int i = 0; i < 8; i++) {
				if(new Random().nextInt(probRilascio) < 1 && pt != null) {
					try {
						Alfa alfa = new Alfa(this.space,this.grid, pt, i*45);
						ContextUtils.getContext(this).add(alfa);
						alfa.build();
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
			this.stato = StatoInterno.malato;
			this.updateNetwork();
		}
	}


//	/**
//	 * Controlla se sono presenti tra le celle vicine citochine
//	 */
//	@Watch (watcheeClassName = "parkinson.Citochina",
//			watcheeFieldNames = "moved",
//			query = "within_moore 1",
//			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
//	public void checkCito() {
//
//		GridPoint pt = grid.getLocation(this);
//		GridCellNgh<Citochina> n = new GridCellNgh<>(grid, pt, Citochina.class, 1,1);
//
//		List<GridCell<Citochina>> nn = n.getNeighborhood(true);
//
//		Context<Object> context = ContextUtils.getContext(true);
//
//
//		for (GridCell<Citochina> gridCell : nn) {
//			if(!gridCell.getPoint().equals(pt)) {
//				Iterable<Citochina> iter = gridCell.items();
//				for (Citochina c : iter) {	
//					resistenza++;
//					context.remove(c);
//				}			
//			}
//
//		}
//		if(resistenza >= nAlfaMalato) {
//			this.alfaRelease(); // sempre se è maggiore o uguale ad 8 altrimenti non rilascia
//			resistenza = 0;
//		}
//
//	}

//	@Watch (watcheeClassName = "parkinson.Alfa",
//			watcheeFieldNames = "moved",
//			query = "within_moore 1",
//			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
//	public void checkExAlfa() {
//		GridPoint pt = grid.getLocation(this);
//		GridCellNgh<Alfa> n = new GridCellNgh<Alfa>(grid, pt, Alfa.class, 1,1);
//		List<GridCell<Alfa>> nn = n.getNeighborhood(true);
//		Context<Object> context = ContextUtils.getContext(true);
//		for (GridCell<Alfa> gridCell : nn) {
//			if(!gridCell.getPoint().equals(pt)) {
//				Iterable<Alfa> iter = gridCell.items();
//				for (Alfa a : iter) {
//					context.remove(a);
//					this.contaAlfa++;				
//				}
//				if(this.contaAlfa >= nAlfaAccumulo) {		
//					stato = StatoInterno.accumulo;
//				}
//				if(this.contaAlfa>= nAlfaMalato) {
//					this.alfaRelease();
//				}
//			}
//		}	
//	}
	
	private void updateNetwork() {
		Iterable<RepastEdge<Object>> edges = this.network.getEdges(this);

		for (RepastEdge<Object> edge : edges) {
			// aggiunge dopamina
			deadNetwork.addEdge(edge);
			network.removeEdge(edge);
		}
	}
}












