package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Citochina {
	
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private NdPoint startAt;
	
	private double angolo;
	private boolean moved= false;
	
	public Citochina (ContinuousSpace<Object> space, Grid<Object> grid, NdPoint startAt ) {	
		this.space = space;
		this.grid = grid;
		this.startAt = startAt;
		this.space.moveTo(this, startAt.getX(),startAt.getY());
		this.grid.moveTo(this, (int) startAt.getX(), (int) startAt.getY());
	}
	
	public Citochina (ContinuousSpace<Object> space, Grid<Object> grid) {	
		this.space = space;
		this.grid = grid;
	}
	
	
	@ScheduledMethod(interval=1)
	public void move(double angolo) {
		this.angolo = angolo;
		this.moved = true;
		// muovo l'oggetto sempre lungo lo stesso angolo
		this.space.moveByVector(this,1 , Math.toRadians(this.angolo), 0);
		this.grid.moveByVector(this ,1,Math.toRadians(this.angolo) ,0);
		
	}
}
