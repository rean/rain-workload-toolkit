	package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class CreateUserOperation extends ScadrOperation 
{
	public static String NAME = "CreateUser";
	public CreateUserOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = ScadrGenerator.CREATE_USER;
	}

	@Override
	public void execute() throws Throwable 
	{
		this.doCreateUser();
		this.setFailed( false );
	}

}
