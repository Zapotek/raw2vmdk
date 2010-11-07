
package segfault.raw2vmdk;

/*
 * Raw2VMDK.java Copyright (C) 2010 Anastasios Laskos <tasos.laskos@gmail.com>
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

import java.io.File;
import java.util.*;

import cert.forensics.mbr.MasterBootRecord;

/**
 * <p>Main class.</p>
 * <p>Drives the cert.forensics.mbr.MasterBootRecord and
 * segfault.raw2vmdk.VMDKTemplate classes.</p>
 *
 * @author zapotek <zapotek@segfault.gr>
 * @version 0.1.3.1
 *
 * @see cert.forensics.mbr.MasterBootRecord
 * @see segfault.raw2vmdk.VMDKTemplate
 */
public class Raw2VMDK {

    /**
     * The version of the application.<br/>
     * Used in {@link #banner()}
     */
    private static final String VERSION = "0.1.3.1";

    /**
     * The SVN revision of the application.<br/>
     * Used in {@link #banner()}
     */
    private static final String SVN_REV = "$Rev$";

    /**
     * total number of sectors
     */
    static long                 numOfSectors;

    /**
     * total number of cylinders
     */
    static long                 numOfCylinders;

    /**
     *  heads per track
     */
    static int                  headsPerTrack;

    /**
     *  sectors per track
     */
    static long                 sectorsPerTrack;

    /**
     * the VMDK template
     */
    static String               tpl     = "vmdk.tpl";

    /**
     *  the location of the raw image
     */
    static String               imageLocation;

    /**
     * <p>Main method</p>
     * <p>Drives the cert.forensics.mbr.MasterBootRecord and
     * segfault.raw2vmdk.VMDKTemplate classes.</p>
     *
     * @param args  <raw image> <vmdk outfile>
     *
     * @see cert.forensics.mbr.MasterBootRecord
     * @see segfault.raw2vmdk.VMDKTemplate
     */
    public static void main( String[] args ) {

        banner( );

        if( args.length == 0 ) {
            usage( );
            return;
        }

        if( args.length < 2 ) {
            usage( );
            System.out.println( System.getProperty( "line.separator" )
                    + "Error: raw2vmdk expects at least 2 arguments." );
            return;
        }

        String diskType = System.getProperty( "type", "ide" );
        String[] acceptedTypes = { "ide", "buslogic", "lsilogic", "legacyESX" };

        if( !inArray( acceptedTypes, diskType ) ) {
            System.out.println( System.getProperty( "line.separator" )
                    + "Error: Disk type is incorrect." );
            usage( );
            return;
        }

        imageLocation   = args[0];

        File imgFile    = new File( imageLocation );

        // check if the raw image file exists
        if( !imgFile.exists( ) ) {
            System.out.println( System.getProperty( "line.separator" )
                    + "Error: Image file does not exist." );
            return;
        }

        System.out.print( "Analysing image:"
                + System.getProperty( "line.separator" ) + imageLocation );

        // analyse the image
        MasterBootRecord MBR = new MasterBootRecord( imgFile );

        System.out.println( " [" + MBR.getFileSizeBytes( ) + " bytes]" );
        System.out.println( );

        numOfSectors = MBR.totalSectorsOnDiskFromFile( );
        System.out.println( "Number of sectors:\t" + numOfSectors );

        numOfCylinders = MBR.largestCylinderValOnDisk( );
        System.out.println( "Number of cylinders:\t" + numOfCylinders );

        headsPerTrack = MBR.getPartitionEntry1( ).getNumHeads( );
        System.out.println( "Heads per track:\t" + headsPerTrack );

        sectorsPerTrack = MBR.getPartitionEntry1( ).getEndSector( );
        System.out.println( "Sectors per track:\t" + sectorsPerTrack );

        // create hashmap holding data for the VMDK template
        HashMap<String, String> vmdkData = new HashMap<String, String>( );

        vmdkData.put( "diskType", diskType );
        vmdkData.put( "numOfSectors", Long.toString( numOfSectors ) );
        vmdkData.put( "numOfCylinders", Long.toString( numOfCylinders ) );
        vmdkData.put( "headsPerTrack", Integer.toString( headsPerTrack ) );
        vmdkData.put( "sectorsPerTrack", Long.toString( sectorsPerTrack ) );
        vmdkData.put( "imgLocation", imageLocation );

        System.out.println( System.getProperty( "line.separator" )
                + "Loading VMDK template..." );

        try {
            // load VMDK template file
            VMDKTemplate vmdkTpl = new VMDKTemplate( tpl );

            System.out.print( "Writing VMDK file to: " );

            // write VMDK file to disk
            vmdkTpl.write( vmdkData, args[1] );
            System.out.println( args[1] );
        } catch( Exception e ) {
            System.out.println( System.getProperty( "line.separator" )
                    + "Error: " + e.getMessage( ) );
            return;
        }

        System.out
                .println( "All done." + System.getProperty( "line.separator" ) );
    }

    /**
     * Outputs the banner message of the application
     */
    public static void banner( ) {

        System.out.println( "raw2vmdk " + VERSION + " [" + SVN_REV
                + "] initiated." );
        System.out.println( "   Author: Zapotek <zapotek@segfault.gr>" );
        System.out.println( "   Website: http://www.segfault.gr" );
        System.out.println( );
    }

    /**
     * Outputs usage info
     */
    public static void usage( ) {

        System.out.println( );
        System.out.println( "Usage:" );
        System.out
            .println( "java -jar -Dtype=<ide|buslogic|lsilogic|legacyESX> raw2vmdk.jar <raw image> <vmdk outfile>" );

        System.out.println( "\ntype defaults to 'ide'" );
    }

    private static Boolean inArray( String[] arr, String str ) {

        for( String s: arr ) {
          if( s.equals( str ) ) return true;
        }

        return false;

    }

}
