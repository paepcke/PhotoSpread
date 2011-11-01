/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadUtilities;

/**
 *
 * @author skandel
 */
public class PhotoSpreadHelpers {
	
	public static enum TagType {
		startTag,
		endTag
	}
    
    
    static public String getXMLElement(String elementName, String elementValue){
        StringBuffer xml = new StringBuffer();
        
        xml.append("<" + elementName + ">");
        xml.append(Misc.escapeXMLSpecials(elementValue));
        xml.append("</" + elementName + ">" + System.getProperty("line.separator") );
        
        return xml.toString();
    }
    
     static public String getXMLElement(String elementName, int elementValue){
         return getXMLElement(elementName, new Integer(elementValue).toString());
     
     }
    
     static public String getXMLElement(String elementName, TagType startOrEndTag){
        StringBuffer xml = new StringBuffer();
        switch (startOrEndTag) {
        case startTag:
            xml.append("<" + elementName + ">" + System.getProperty("line.separator") );
            break;
        case endTag:
        	xml.append("</" + elementName + ">" + System.getProperty("line.separator") );
        	break;
        }
        return xml.toString();
     
     }
}
