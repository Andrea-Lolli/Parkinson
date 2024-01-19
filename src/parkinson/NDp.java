package parkinson;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class NDp {
	
	// TODO spostare in contextBuilder
	// Parametri simulazione 
	private int nAlfaAccumulo = 4; // Soglie stati interni
	private int nAlfaMalato = 12;
	private int probAccumuloAlfa = 5; // (1/probAccumuloAlfa) = probabilità di incrementare alfasinucleina interna
	private int probRilascio = 5; // (1/proRilascio) = probabilità di generare agenti in ognuna delle direzioni
	private static int reach = 5; // raggio percezione
	
	public enum StatoInterno{
		produzione, accumulo, malato;
	}

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Object> network;
	private Network<Object> deadNetwork;

	// descrive lo stato interno.
	private StatoInterno stato;
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

	// Colori per display
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
	 * Rilascio alfasinucleina quando muore il neurone
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

	/**
	 * Assorbe citochine che danneggiano il neurone
	 */
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
					if(stato!=StatoInterno.malato) {
						context.remove(agent);
						resistenza++;
						if(resistenza >= nAlfaMalato) {
							this.stato = StatoInterno.malato;
							updateNetwork();
							this.alfaRelease(); // sempre se è maggiore o uguale ad 8 altrimenti non rilascia
							resistenza = 0;
						}
					}
				}	
			}
		}
	}
	
	/**
	 * Assorbe alfa vicina
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
					if(stato!=StatoInterno.malato) {
						//System.out.println("c'è della alfaa vicino a me!!!");
						context.remove(agent);
						contaAlfa++;
						
						if(this.contaAlfa >= nAlfaAccumulo) {		
							stato = StatoInterno.accumulo;
						}
						if(this.contaAlfa>= nAlfaMalato) {
							this.alfaRelease();
							this.stato = StatoInterno.malato;
							updateNetwork();
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
	
    /**
	 * Aggiorna la network quando il neurone muore
	 */
	private void updateNetwork() {
		Iterable<RepastEdge<Object>> edges = this.network.getEdges(this);

		for (RepastEdge<Object> edge : edges) {
			// aggiunge dopamina
			deadNetwork.addEdge(edge);
			network.removeEdge(edge);
		}
	}
}












