package segfault.raw2vmdk;

/*
 * $Id$
 * 
 * Raw2VMDK.java Copyright (C) 2006-2008 Anastasios Laskos
 *                                       <tasos.laskos@gmail.com>
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
 * Main class
 * Drives the cert.forensics.mbr.MasterBootRecord and
 * segfault.raw2vmdk.VMDKTemplate classes.
 * 
 * @author zapotek <zapotek@segfault.gr>
 * 
 */
public class Raw2VMDK {

    private static final String VERSION = "0.1";

    private static final String SVN_REV = "$Rev$";

    // total number of sectors
    static long   numOfSectors;

    // total number of cylinders
    static long   numOfCylinders;

    // heads per track
    static int    headsPerTrack;

    // sectors per track
    static long   sectorsPerTrack;

    // the VMDK template
    static String tpl = "vmdk.tpl";

    // the location of the raw image
    static String imageLocation;

    /**
     * @param args
     */
    public static void main( String[] args ) {

        banner( );
        
        if( args.length == 0 ) {
            usage( );
            return;
        }
        
        if( args.length != 2 ) {
            usage( );
            System.out.println( "\nError: raw2vmdk expects exactly 2 arguments." );
            return;
        }
        
        imageLocation = args[0];
        File imgFile = new File( imageLocation );
        
        // check if the raw image file exists
        if( !imgFile.exists( ) ){
            System.out.println( "\nError: Image file does not exist." );
            return;
        }
        
        System.out.print( "Analysing image:\n" + imageLocation );
        
        // analyse the image
        MasterBootRecord MBR = new MasterBootRecord( imgFile );
        
        System.out.println( " [" + MBR.getFileSizeBytes( ) + " bytes]" );
        System.out.println();
        
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

        vmdkData.put( "numOfSectors", Long.toString( numOfSectors ) );
        vmdkData.put( "numOfCylinders", Long.toString( numOfCylinders ) );
        vmdkData.put( "headsPerTrack", Integer.toString( headsPerTrack ) );
        vmdkData.put( "sectorsPerTrack", Long.toString( sectorsPerTrack ) );
        vmdkData.put( "imgLocation", imageLocation );

        System.out.println( "\nLoading VMDK template...");

        // load VMDK template file
        VMDKTemplate vmdkTpl = new VMDKTemplate( tpl );
        
        System.out.print( "Writing VMDK file to: " );
        
        // write VMDK file to disk
        vmdkTpl.write( vmdkData, args[1] );
        System.out.println( args[1] );
        
        System.out.println( "All done.\n" );
    }
    
    /**
     * 
     */
    public static void banner( ) {
        System.out.println( "raw2vmdk " + VERSION + " [" + SVN_REV + "] initiated." );
        System.out.println( "   Author: Zapotek <zapotek@segfault.gr>" );
        System.out.println( "   Website: http://www.segfault.gr" );
        System.out.println( );
    }
    
    /**
     * 
     */
    public static void usage( ) {
        System.out.println( );
        System.out.println( "Usage:" );
        System.out.println( "java -jar raw2vmdk.jar <raw image> <vmdk outfile>" );
    }

}
