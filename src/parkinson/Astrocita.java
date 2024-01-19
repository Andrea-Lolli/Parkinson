package parkinson;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Astrocita {
	
	private int probRilascio = 20; //probabilità di generare molMod in ognuna delle 8 direzioni
	
	private enum StatoInterno {
		attivo, inattivo
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
	// descrive lo stato interno.
	private StatoInterno stato;
	private int contaCito = 0;
	
	// gli astrociti vanno creati dopo i microglia
	public Astrocita(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid=grid;	
		this.stato = StatoInterno.inattivo;
	}
	
	// Chiedere ad alessio come funziona questo metodo (gli stati sono messi a cazzo...)
	public void releaseMolMod() {
		if(stato==StatoInterno.inattivo) {	
			GridPoint pt = grid.getLocation(this);
			for (int i = 0; i < 8; i++) {
				if(new Random().nextInt(probRilascio) < 1 && pt != null) {
					MolMod mol = new MolMod(space, grid, pt, i*45);
					ContextUtils.getContext(this).add(mol);
					mol.build();
				}
			}
		}
	}
	
//	// equilibrio |Astrociti| = |Microglia|
//	@ScheduledMethod(start = 1, interval=1)
//	public void modulazione() {
//		if(stato == StatoInterno.attivo) {
//			for (int i = 0; i < 8; i++) {
//				MolMod mm = new MolMod(this.space, this.grid, this.pt );
//				ContextUtils.getContext(this).add(mm);
//				mm.move(i*45);		
//			}
//		}		
//	}
	
	@Watch (watcheeClassName = "parkinson.Citochina",
			watcheeFieldNames = "moved",
			whenToTrigger = WatcherTriggerSchedule.LATER)
	public void checkCito(Citochina agent) {
		//System.out.println("se movee");
		GridPoint agentPos = agent.getPosition();
		
		int x = agentPos.getX();
		int y = agentPos.getY();
		
		Context<Object> context = ContextUtils.getContext(this);
		
		if(context != null) {
			GridPoint pos = grid.getLocation(this);
			if(pos != null) {
				int x1 = pos.getX();
				int y1 = pos.getY();
				if(isEuclideanDistanceLessThanFive(x,y,x1,y1)) {
					if(stato==StatoInterno.inattivo) {
						context.remove(agent);
						contaCito++;
						releaseMolMod();
						//System.out.println("ciaooo");
						
						//Chiedere a alessio come deve funzionare esattamente
//						if(contaCito > 0) {
//							stato=StatoInterno.attivo;
//							contaCito = 0;
//						}else {
//							stato=StatoInterno.inattivo;
//						}
					}
				}	
			}
		}
	}
	
    private static boolean isEuclideanDistanceLessThanFive(int x1, int y1, int x2, int y2) {
        // Calcola la distanza euclidea tra i due punti
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        // Verifica se la distanza è inferiore a 5
        return distance < 5.0;
    }
	
//	@Watch ( watcheeClassName = "parkinson.Citochina" ,
//			 watcheeFieldNames = "moved" ,
//			 query = "within_moore 1" ,
//			 whenToTrigger = WatcherTriggerSchedule.LATER )
//	public void checkCito() {
//		
//		GridCellNgh<Citochina> n = new GridCellNgh<>(this.grid, this.pt, Citochina.class, 1,1);
//		List<GridCell<Citochina>> nn = n.getNeighborhood(true);
//		Context<Object> context = ContextUtils.getContext(true);
//		if (!nn.isEmpty()) {
//			for (GridCell obj : nn) {
//				Iterable<Citochina> c = obj.items();
//				for (Iterator iterator = c.iterator(); iterator.hasNext();) {
//					contaCito++;
//					context.remove(iterator.next());
//				}
//			}
//		}
//		if(contaCito > 0) {
//			stato=StatoInterno.attivo;
//			contaCito = 0;
//		}else {
//			stato=StatoInterno.inattivo;
//		}
//	}
}
