package radlab.rain;

import java.util.LinkedList;

//import java.util.LinkedList;

public abstract class LoadScheduleCreator implements ILoadScheduleCreator 
{
	public abstract LinkedList<LoadProfile> createSchedule(); 
}
