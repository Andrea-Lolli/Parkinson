package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Alfa {
		
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint pt;
	private double angolo;
	private boolean moved = false;
	
	public Alfa (ContinuousSpace<Object> space, Grid<Object> grid, GridPoint startAt ) {	
		this.space = space;
		this.grid = grid;
		this.pt = startAt;
		this.space.moveTo(this, startAt.getX(),startAt.getY());
		this.grid.moveTo(this, startAt.getX(),startAt.getY());
	}
	
	@ScheduledMethod(interval = 1)
	public void move(double angolo) {
		this.moved = true;
		//salvo la direzione dell'obj
		this.angolo = angolo;
		// muovo l'oggetto sempre lungo lo stesso angolo
		this.space.moveByVector(this,1 , Math.toRadians(this.angolo), 0);
		this.grid.moveByVector(this ,1,Math.toRadians(this.angolo) ,0);
	}
	
}
