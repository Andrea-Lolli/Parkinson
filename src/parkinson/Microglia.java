package parkinson;


import java.util.Iterator;
import java.util.List;

import parkinson.NDp.StatoInterno;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Microglia {
		
	
	private enum StatoInterno {
		attivo, inattivo
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
	
	// descrive lo stato interno.
	private StatoInterno stato;
	
	private final int soglia = 8;
	
	private int contaAlfa = 0;
	private int contaStress = 0;
	private int contaMolMod = 0;
	
	
	public Microglia(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.stato = StatoInterno.inattivo;
	}
	
	
	// una volta attivato il rilascio è periodico (?)
	@ScheduledMethod(interval = 2)
	public void releaseCito() {
		if(stato==StatoInterno.attivo) {
			for (int i = 0; i < contaStress; i++) {
				NdPoint pt1 = space.getLocation(this);
				Citochina c = new Citochina(space, grid, pt1);
				ContextUtils.getContext(this).add(c);
				c.move(i*45);
			}
			for (int i = 0; i < contaAlfa; i++) {
				NdPoint pt2 = space.getLocation(this);
				Citochina c = new Citochina(space, grid, pt2);
				ContextUtils.getContext(this).add(c);
				c.move(i*45);
			}
			
			if(contaAlfa > 8 ) contaAlfa = 8;
			if(contaStress > 8) contaStress = 8;
		}
		
		
	}

	@Watch ( watcheeClassName = "parkinson.Alfa",
			 watcheeFieldNames = " moved " ,
			 query = " within_moore 1 " ,
			 whenToTrigger = WatcherTriggerSchedule . IMMEDIATE )
	public void checkAlfa() {
		GridCellNgh<Alfa> n = new GridCellNgh<>(this.grid, this.pt, Alfa.class, 1,1);
		List<GridCell<Alfa>> nn = n.getNeighborhood(true);
		Context<Object> context = ContextUtils.getContext(true);
			for (GridCell<Alfa> gridCell : nn) {
				if( !gridCell.equals(this) ) {
					Iterable<Alfa> iter = gridCell.items();
					for (Alfa a : iter) {
						contaAlfa++;
						context.remove(a);
					}
				}
			}
			if(contaAlfa > soglia) {
				this.stato= StatoInterno.attivo;
				this.releaseCito(); // sempre se è maggiore o uguale ad 8 altrimenti non rilascia
			}	
	}
	
	@Watch ( watcheeClassName = "parkinson.StressSign",
			 watcheeFieldNames = " moved " ,
			 query = " within_moore 1 " ,
			 whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void checkStress() {
		
		GridCellNgh<StressSign> n = new GridCellNgh<>(this.grid, this.pt, StressSign.class, 1,1);
		List<GridCell<StressSign>> nn = n.getNeighborhood(true);
		Context<Object> context = ContextUtils.getContext(true);
		for (GridCell<StressSign> gridCell : nn) {
			if(!gridCell.equals(this)) {
				Iterable<StressSign> iter = gridCell.items();
				for (StressSign c : iter) {
					contaStress++;
					context.remove(c);
				}		
		}}
		
		if(contaStress > soglia) {
			this.stato= StatoInterno.attivo;
			this.releaseCito(); // sempre se è maggiore o uguale ad 8 altrimenti non rilascia
		}
	}
	
	
	@Watch ( watcheeClassName = "parkinson.MolMod" ,
			 watcheeFieldNames = " moved " ,
			 query = " within_moore 1 " ,
			 whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
	public void disable() {
		
		// basta avere una molecola di modulazione per disattivare il microglia
		
		GridCellNgh<MolMod> n = new GridCellNgh<>(this.grid, this.pt, MolMod.class, 1,1);
		List<GridCell<MolMod>> nn = n.getNeighborhood(true);
		Context<Object> context = ContextUtils.getContext(true);
		if (!nn.isEmpty()) {
			for (GridCell obj : nn) {
				Iterable<MolMod> mm = obj.items();
				for (Iterator iterator = mm.iterator(); iterator.hasNext();) {
					contaMolMod++;
					context.remove(iterator.next());
				}
			}
		}
		if (contaMolMod > 1) {
			contaStress = 0;
			contaAlfa = 0;
			stato = stato.inattivo;
		}
	}
	
}
