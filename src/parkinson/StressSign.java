package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class StressSign {
		
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint startAt;
	
	private double angolo;
	public boolean moved = false;
	private int stepNumber = 0;
	private boolean settedUp = false;
	
	public StressSign (ContinuousSpace<Object> space, Grid<Object> grid, GridPoint pt, double angolo) {	
		this.space = space;
		this.grid = grid;
		this.startAt = pt;
		this.angolo = angolo;
	}	
	
	public void build() {
		this.space.moveTo(this, startAt.getX(), startAt.getY());
		this.grid.moveTo(this, startAt.getX(), startAt.getY());
		settedUp = true;
	}
	
	public GridPoint getPosition() {
		return startAt;
	}
	
	@ScheduledMethod(start = 2, interval=1)
	public void move() {
		if(!settedUp)
			return;
		
		if(stepNumber == 5) {
			try {
				ContextUtils.getContext(this).remove(this);
			} catch(Exception e) {
				System.out.println("aaaaaaaa" + e.getMessage());
			}
			return;
		}
		
		this.stepNumber++;
		int newX = startAt.getX() + getValueX();
		int newY = startAt.getY() + getValueY();
		
		this.space.moveTo(this, newX, newY);
		this.grid.moveTo(this, newX, newY);
		// muovo l'oggetto sempre lungo lo stesso angolo
		this.startAt = new GridPoint(newX, newY);
		this.moved = true;
	}
	
	private int getValueX() {
		if(angolo == 0 || angolo == 45 || angolo == 315)
			return 1;
		if(angolo == 90 || angolo == 270)
			return 0;
		return -1;
	}
	
	private int getValueY() {
		if(angolo == 45 || angolo == 90 || angolo == 135)
			return 1;
		if(angolo == 0 || angolo == 180)
			return 0;
		return -1;
	}
			
}
