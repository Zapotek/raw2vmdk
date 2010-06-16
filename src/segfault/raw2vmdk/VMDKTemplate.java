
package segfault.raw2vmdk;

/*
 * $Id$
 * 
 * VMDKTemplate.java Copyright (C) 2010 Anastasios Laskos
 * <tasos.laskos@gmail.com>
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Template manager.</p>
 * <p>Loads the default VMDK template file, evaluates it and
 * creates new VMDK file based on {@link #write(HashMap, String)} parameters.</p>
 * 
 * @author zapotek <zapotek@segfault.gr>
 * @version 0.1.1
 */
public class VMDKTemplate {

    /**
     *  The VMDK template file
     */
    File tplFile;

    /**
     * Constructor <br/>
     * Initialises {@link #tplFile} using the tplLocation parameter
     * 
     * @param tplLocation the location of the template file
     */
    public VMDKTemplate( String tplLocation ) throws Exception {

        this.tplFile = new File( tplLocation );
        
        if( !tplFile.exists( ) ) {
            throw new Exception( "Template file does not exist." );
        }
    }

    /**
     * Creates new VMDK file "outFile" from the template in "tpl" with the values
     * of tplData
     * 
     * @param tplData   the template data data in a tplVarName => value
     * @param outFile   where to write the .vmdk file
     */
    public void write( HashMap<String, String> tplData, String outFile ) {

        try {
//            File tplFile = new File( tpl );
            BufferedReader reader = new BufferedReader(
                    new FileReader( tplFile ) );

            String line = "", tplText = "";

            // read the template file and store it in vmdkTpl
            while( ( line = reader.readLine( ) ) != null ) {
                tplText += line + System.getProperty( "line.separator" );
            }

            // System.out.println( tplText );
            reader.close( );

            Iterator<String> it = tplData.keySet( ).iterator( );
            String vmdkText = tplText;

            // replace template variables with actual values
            while( it.hasNext( ) ) {
                String key = (String) it.next( );
                String val = tplData.get( key );

                // System.out.println( key + "::" + val );

                vmdkText = vmdkText.replaceAll( "\\[" + key + "\\]", val );

            }

            // System.out.println( "vmdkText: " + vmdkText );

            FileWriter writer = new FileWriter( outFile );
            writer.write( vmdkText );
            writer.close( );

        } catch( IOException ioe ) {
            ioe.printStackTrace( );
        }
    }

}
