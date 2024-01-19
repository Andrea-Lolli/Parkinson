package parkinson;

import java.util.Random;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Microglia {
		
	// TODO spostare in contextBuilder
	// Parametri simulazione
	private int probRilascio = 5;
	private int soglia = 3;
	private static int reach = 5; //raggio della percezione
	
	private enum StatoInterno {
		attivo, inattivo
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
	private StatoInterno stato;
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

	/*
	 * Assorbe alfa e si attiva
	 */
	@Watch ( watcheeClassName = "parkinson.Alfa",
			 watcheeFieldNames = "moved",
			 whenToTrigger = WatcherTriggerSchedule.LATER)
	public void checkAlfa(Alfa agent) {
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
						//System.out.println("c'è della alfaa vicino a me!!!");
						context.remove(agent);
						contaAlfa++;
						
						if(contaAlfa >= soglia) {
							this.stato= StatoInterno.attivo;
							//System.out.println("MI ATTIVOOO " + x1 + " " + y2);
						}
					}
				}	
			}
		}
	}
	
	/*
	 * Assorbe stress e si attiva
	 */
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
				int y1 = pos.getY();
				if(isEuclideanDistanceLessThanReach(x,y,x1,y1)) {
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
	
	/*
	 * Assorbe MolMod e si disattiva
	 */
	@Watch (watcheeClassName = "parkinson.MolMod",
			watcheeFieldNames = "moved",
			whenToTrigger = WatcherTriggerSchedule.LATER)
	public void disable(MolMod agent) {
		
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
					if(stato==StatoInterno.attivo) {
						context.remove(agent);
						contaMolMod++;
						
						if(contaMolMod >= 1) {
							//System.out.println("Mi disattivooooooo : (");
							this.stato= StatoInterno.inattivo;
						}
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