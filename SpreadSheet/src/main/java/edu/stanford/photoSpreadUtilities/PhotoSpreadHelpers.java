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
    
     static public String getXMLElement(String elementName, Boolean startTag){
        StringBuffer xml = new StringBuffer();
        if(startTag){
            xml.append("<" + elementName + ">" + System.getProperty("line.separator") );
        }
        else{
             xml.append("</" + elementName + ">" + System.getProperty("line.separator") );
        }
        return xml.toString();
     
     }
}
