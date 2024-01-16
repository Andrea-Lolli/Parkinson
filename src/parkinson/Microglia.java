package parkinson;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
		
	// Parametri simulazione
	private int probRilascio = 5;
	private int soglia = 3;
	private int reach = 5; //raggio della percezione
	
	private enum StatoInterno {
		attivo, inattivo
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
	private StatoInterno stato; // descrive lo stato interno.
	private int contaAlfa = 0;
	private int contaStress = 0;
	private int contaMolMod = 0;
		
	public Microglia(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.stato = StatoInterno.inattivo;
	}
	
	// una volta attivato il rilascio è periodico (?)
	// UPDATED
	@ScheduledMethod(start = 1, interval = 2)
	public void releaseCito() {
		if(stato==StatoInterno.attivo) {	
			GridPoint pt = grid.getLocation(this);
			for (int i = 0; i < 8; i++) {
				if(new Random().nextInt(probRilascio) < 1 && pt != null) {
					Citochina cito = new Citochina(space, grid, pt, i*45);
					ContextUtils.getContext(this).add(cito);
					cito.build();
				}
			}
		}
	}

	@Watch ( watcheeClassName = "parkinson.Alfa",
			 watcheeFieldNames = "moved",
			 whenToTrigger = WatcherTriggerSchedule.LATER)
	public void checkAlfa() {
		System.out.println("percepisco alfaa");
//		pt = (pt == null) ? this.pt = grid.getLocation(this):pt;
//		if(stato==StatoInterno.inattivo) {
//		GridPoint pt = grid.getLocation(this);
//		GridCellNgh<Alfa> n = new GridCellNgh<>(this.grid, pt, Alfa.class, 1,1);
//		List<GridCell<Alfa>> nn = n.getNeighborhood(true);
//		Context<Object> context = ContextUtils.getContext(this);
//			for (GridCell<Alfa> gridCell : nn) {
//				if( !gridCell.getPoint().equals(pt) ) {
//					Iterable<Alfa> iter = gridCell.items();
//					for (Alfa a : iter) {
//						contaAlfa++;
//						context.remove(a);
//					}
//				}
//			}
//			if(contaAlfa >= soglia) {
//				this.stato= StatoInterno.attivo;
//			}	
//		}
	}
	
	@Watch (watcheeClassName = "parkinson.StressSign",
			watcheeFieldNames = "moved",
			whenToTrigger = WatcherTriggerSchedule.LATER)
	public void checkStress(StressSign agent) {
		
		GridPoint agentPos = agent.getPosition();
		
		int x = agentPos.getX();
		int y = agentPos.getY();
		
		Context<Object> context = ContextUtils.getContext(this);
		
		if(context != null) {
			GridPoint pos = grid.getLocation(this);
			if(pos != null) {
				int x1 = pos.getX();
				int y2 = pos.getY();
				if(isEuclideanDistanceLessThanFive(x,y,x1,y2)) {
					if(stato==StatoInterno.inattivo) {
						//System.out.println("c'è dello stress vicino a me!!!");
						context.remove(agent);
						contaStress++;
						
						if(contaStress >= soglia) {
							this.stato= StatoInterno.attivo;
							//System.out.println("MI ATTIVOOO " + x1 + " " + y2);
						}
					}
				}	
			}
		}
	}
		
		//System.out.println("stress: " + agentPos.getX() + " " + agentPos.getY());
		
		//if(agent instanceof )
//		pt = (pt == null) ? this.pt = grid.getLocation(this):pt;
//		if(stato==StatoInterno.inattivo) {
//		GridPoint pt = grid.getLocation(this);
//		GridCellNgh<StressSign> n = new GridCellNgh<>(this.grid, pt, StressSign.class, 1,1);
//		List<GridCell<StressSign>> nn = n.getNeighborhood(true);
//		Context<Object> context = ContextUtils.getContext(this);
//		for (GridCell<StressSign> gridCell : nn) {
//			if(!gridCell.getPoint().equals(pt)) {
//				Iterable<StressSign> iter = gridCell.items();
//				for (StressSign c : iter) {
//					context.remove(c);
//					contaStress++;
//				}		
//			}
//		}
//			if(contaStress >= soglia) {
//				this.stato= StatoInterno.attivo;
//			}
//		}
	
    private static boolean isEuclideanDistanceLessThanFive(int x1, int y1, int x2, int y2) {
        // Calcola la distanza euclidea tra i due punti
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        // Verifica se la distanza è inferiore a 5
        return distance < 5.0;
    }

//	@Watch ( watcheeClassName = "parkinson.MolMod",
//			 watcheeFieldNames = "moved",
//			 query = "within_moore 1",
//			 whenToTrigger = WatcherTriggerSchedule.LATER)
//	public void disable() {
//		pt = (pt == null) ? this.pt = grid.getLocation(this):pt;
//		if(stato==StatoInterno.attivo) {
//		GridPoint pt = grid.getLocation(this);
//		GridCellNgh<MolMod> n = new GridCellNgh<>(this.grid, pt, MolMod.class, 1,1);
//		List<GridCell<MolMod>> nn = n.getNeighborhood(true);
//		Context<Object> context = ContextUtils.getContext(this);
//		for (GridCell<MolMod> gridCell : nn) {
//			if(!gridCell.getPoint().equals(pt)) {
//				Iterable<MolMod> iter = gridCell.items();
//				for (MolMod c : iter) {
//					contaMolMod++;
//					context.remove(c);
//				}		
//			}
//		}
//			if (contaMolMod >= 1) {
//				stato = stato.inattivo;
//			}
//		}
//	}
}