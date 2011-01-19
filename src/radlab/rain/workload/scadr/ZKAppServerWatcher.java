package radlab.rain.workload.scadr;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class ZKAppServerWatcher implements Watcher 
{
	ScadrGenerator _generator = null;
	
	public ZKAppServerWatcher( ScadrGenerator generator ) 
	{
		this._generator = generator;
	}

	@Override
	public void process( WatchedEvent event ) 
	{
		// Get the new list of urls, and pass them to the generator
		//this._generator.setAppServerList( )
		if( event.getType() == Watcher.Event.EventType.NodeDataChanged )
			this._generator.setAppServerListChanged( true );
	}

}
