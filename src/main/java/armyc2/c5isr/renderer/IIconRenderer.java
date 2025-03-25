package armyc2.c5isr.renderer;

import java.util.Map;

import armyc2.c5isr.renderer.utilities.ImageInfo;

/**
 * @deprecated
 * 
 */
public interface IIconRenderer {

	public Boolean CanRender(String symbolID, Map<String,String> modifiers);
	
	public ImageInfo RenderIcon(String symbolID, Map<String,String> modifiers);
	
	public String getRendererID();
	
}
