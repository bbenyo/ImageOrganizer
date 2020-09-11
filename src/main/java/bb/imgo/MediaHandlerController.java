package bb.imgo;

import java.util.List;

import bb.imgo.handlers.MediaHandler;

public interface MediaHandlerController {
	
	public List<MediaHandler> getHandlers();
	
	public MediaHandler getSpecificHandler(Class<?> handlerClass);
	
}
