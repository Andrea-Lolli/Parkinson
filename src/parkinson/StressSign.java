package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class StressSign {
		
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint startAt;
	
	private double angolo;
	private boolean moved = false;
	
	public StressSign (ContinuousSpace<Object> space, Grid<Object> grid, GridPoint pt ) {	
		this.space = space;
		this.grid = grid;
		this.startAt = pt;
		this.space.moveTo(this, startAt.getX(),startAt.getY());
		this.grid.moveTo(this, startAt.getX(),startAt.getY());
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
