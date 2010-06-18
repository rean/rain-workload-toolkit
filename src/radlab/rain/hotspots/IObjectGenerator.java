package radlab.rain.hotspots;

import java.util.List;

public interface IObjectGenerator<E> {
	public abstract E next();
	public abstract Integer numberOfObjects();
	public abstract List<E> objects();
}
