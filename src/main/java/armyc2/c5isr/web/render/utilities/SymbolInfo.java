/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.web.render.utilities;

import java.util.ArrayList;

/**
 *
 * 
 */
public class SymbolInfo {
    
    private ArrayList<LineInfo> _LineInfo = null;
    private ArrayList<TextInfo> _TextInfo = null;
    
    public SymbolInfo()
    {
        
    }
    public SymbolInfo(ArrayList<TextInfo> ti, ArrayList<LineInfo> li)
    {
        _LineInfo = li;
        _TextInfo = ti;
    }
    
    public ArrayList<TextInfo> getTextInfoList()
    {
        return _TextInfo;
    }
    
    public ArrayList<LineInfo> getLineInfoList()
    {
        return _LineInfo;
    }
    
}
