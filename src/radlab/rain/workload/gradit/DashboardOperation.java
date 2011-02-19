package radlab.rain.workload.gradit;

import radlab.rain.IScoreboard;

public class DashboardOperation extends GraditOperation 
{
	public static String NAME = "Dashboard";
	
	public DashboardOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = GraditGenerator.DASHBOARD;
	}
	
	@Override
	public void execute() throws Throwable 
	{
		this.doDashboard();
		this.setFailed( false );
	}

}
