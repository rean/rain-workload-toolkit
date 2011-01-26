package radlab.rain.workload.scadr;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class ZKAppServerWatcher implements Watcher 
{
	ScadrScenarioTrack _track = null;
		
	public ZKAppServerWatcher( ScadrScenarioTrack track ) 
	{
		this._track = track;
	}

	@Override
	public void process( WatchedEvent event ) 
	{
		// Get the new list of urls, and pass them to the generator
		//this._generator.setAppServerList( )
		if( event.getType() == Watcher.Event.EventType.NodeDataChanged )
			this._track.setAppServerListChanged( true );
	}
}
