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
	
	// TODO spostare in contextBuilder
	// Parametri simulazione
	private int probRilascio = 20; // (1/probRilascio) = probabilità di generare molMod in ognuna delle 8 direzioni
	private static int reach = 5; // raggio percezione
	
	private enum StatoInterno {
		attivo, inattivo
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
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
				if(isEuclideanDistanceLessThanReach(x,y,x1,y1)) {
					if(stato==StatoInterno.inattivo) {
						context.remove(agent);
						contaCito++;
						releaseMolMod();
					}
				}	
			}
		}
	}
	
    private static boolean isEuclideanDistanceLessThanReach(int x1, int y1, int x2, int y2) {
        // Calcola la distanza euclidea tra i due punti
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return distance < reach;
    }
}
