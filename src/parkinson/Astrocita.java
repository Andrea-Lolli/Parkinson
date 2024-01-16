package parkinson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Astrocita {
	
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
