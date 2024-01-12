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
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.query.space.grid.GridCell;

public class NDp {

	public enum StatoInterno{
		produzione, accumulo, malato;
	}
	
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
	private Network<Object> network;
	
	// descrive lo stato interno.
	private StatoInterno stato;
	
	
	private List<Alfa> buffer;
	
	// contro le citochine
	private int resistenza;

	
	public NDp (ContinuousSpace<Object> space, Grid<Object> grid, Network<Object> network) {
		this.space = space;
		this.grid = grid;
		this.stato = StatoInterno.produzione;
		this.buffer = new ArrayList<>();
		this.network = network;
	}
	
	public void autoBind(Context context) {
		
		if(context == (null)) {
			return;
		}
		
		GridPoint pt = grid.getLocation(this);
		
		GridCellNgh<NDp> nghCellNgh = new GridCellNgh<>(grid, pt, NDp.class, 10, 10);
		
		List<GridCell<NDp>> gridCells = nghCellNgh.getNeighborhood(true);
		
		for(GridCell<NDp> cell : gridCells) {
			if(cell.size() == 0)
				break;
			System.out.print("found edge" + "\n");
			cell.items().forEach( neuron -> network.addEdge(this, neuron));
		}
		
		
//		for(Object obj : context) {
//			System.out.print("finding connections..." + "\n");
//			
//			if(obj instanceof NDp) {
//				double d = space.getDistance(space.getLocation(this), space.getLocation(obj));
//				System.out.print("distance found with neuron = " + d + "\n");
//				if(d < 3) 
//					this.network.addEdge(this, obj);
//			}
//		}
	}
	
	/**
	 * Metodo produzione Dopamina.java
	 * Passaggio di dopamina sull'arco di collegamento.
	 * Ogni tick dell'ambiente produce dopamina.
	 * Ogni tick c'è la probabilità di creare alfa-sin.
	 */
//	@ScheduledMethod(start = 1, interval = 1, priority = 2)
//	public void dopRelease() {		
//		
//		//flag controllo stato
//		if(this.stato == StatoInterno.produzione) {
//			
//		Iterable<Object> items = this.network.getAdjacent(this);
//		
//		 for (Object neighbor : items) {
//			 // aggiunge dopamina
//			 ((NeuroneMotorio)neighbor).addDop();
//	     }
//		 
//		}
//	}
//	
//	
//	@ScheduledMethod(start = 1, interval = 2, priority = 1)
//	public void accumulo() {
//		if(stato == StatoInterno.produzione && new Random().nextDouble()< 0.5) {
//			//this.contaAlfa++;
//			Alfa alfa = new Alfa(this.space,this.grid, this.pt);
//			buffer.add(alfa);
//			if(buffer.size() > 3) {		
//			stato = StatoInterno.accumulo;
//			}}		
//	}
//	
//	@ScheduledMethod(start = 1, interval = 3)
//	public void stressRelease () {
//		if(stato==StatoInterno.accumulo) {
//			StressSign ss = new StressSign(space, grid, grid.getLocation(this));
//			ContextUtils.getContext(this).add(ss);
//			ss.move(new Random().nextInt(7)*45);
//		}
//	}
//	
//	// apoptosi simulata
//	@ScheduledMethod(start=1,interval = 1)
//	public void alfaRelease() {
//			if(buffer.size() >= 8) {
//				Context<Object> c = ContextUtils.getContext(this);
//				c.addAll(buffer);				
//				for (int i = 0; i < buffer.size(); i++) {
//					buffer.get(i).move(i*45);
//				}
//				buffer.clear();
//			}
//		   
//	}
//	
//	
//	@Watch ( watcheeClassName = " parkinson.Citochina " ,
//			 watcheeFieldNames = " moved " ,
//			 query = " within_moore 1 " ,
//			 whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
//	public void checkCito() {
//		GridCellNgh<Citochina> n = new GridCellNgh<>(grid, pt, Citochina.class, 1,1);
//		List<GridCell<Citochina>> nn = n.getNeighborhood(true);
//		Context<Object> context = ContextUtils.getContext(true);
//		for (GridCell<Citochina> gridCell : nn) {
//				if(!gridCell.equals(this)) {
//					Iterable<Citochina> iter = gridCell.items();
//					for (Citochina c : iter) {
//					resistenza++;
//					context.remove(c);
//				}	
//				}
//					
//		}
//		if(resistenza > 7) {
//			this.alfaRelease(); // sempre se è maggiore o uguale ad 8 altrimenti non rilascia
//			this.stato= StatoInterno.malato;
//			resistenza = 0;
//		}
//		
//	}
//	
//	@Watch ( watcheeClassName = " parkinson.Alfa " ,
//			 watcheeFieldNames = " moved " ,
//			 query = " within_moore 1 " ,
//			 whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
//	public void checkExAlfa() {
//			GridCellNgh<Alfa> n = new GridCellNgh<Alfa>(grid, pt, Alfa.class, 1,1);
//			List<GridCell<Alfa>> nn = n.getNeighborhood(true);
//			Context<Object> context = ContextUtils.getContext(true);
//			for (GridCell<Alfa> gridCell : nn) {
//				if(!gridCell.equals(this)) {
//					Iterable<Alfa> iter = gridCell.items();
//					for (Alfa a : iter) {
//						context.remove(a);
//						Alfa alfa = new Alfa(this.space,this.grid, this.pt);
//						buffer.add(alfa);
//						if(buffer.size() > 3) {		
//						stato = StatoInterno.accumulo;
//						}}
//				}
//			}		
//	}
	
	
}
