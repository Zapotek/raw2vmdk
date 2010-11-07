
package cert.forensics.mbr;

/*
 * MasterBootRecord.java Copyright (C) 2006-2008 Carnegie Mellon University
 * 
 * Tim Vidas <tvidas at gmail d0t com> Brian Kaplan <bfkaplan at cmu d0t edu>
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MasterBootRecord Represents a 512 byte Master Boot Record for a disk
 * 
 * @author Tim Vidas
 * @author Brian Kaplan
 * @version 0.7, Jan 2009
 */

public class MasterBootRecord {

    private static final int BOOT_CODE_SIZE   = 446;

    private static final int MARKER_SIZE      = 2;

    private static final int BYTES_PER_SECTOR = 512;

    private int[]            bootCode;

    public PartitionEntry    partitionEntry1;

    private PartitionEntry   partitionEntry2;

    private PartitionEntry   partitionEntry3;

    private PartitionEntry   partitionEntry4;

    private int[]            marker;

    private long             fileSizeBytes;

    private int[]            mbrUnsignedBytes;

    /**
     * constructor for MasterBootRecord
     * 
     * @param image
     *            File to a disk image to obtain the mbr from
     */
    public MasterBootRecord( File image ) {

        mbrUnsignedBytes = new int[ 512 ];

        setFileSizeBytes( image.length( ) );

        // read contents of mbr into a 512b mbr buffer
        try {
            InputStream imageStream = new FileInputStream( image );

            /** read mbr into buffer **/
            for( int i = 0; i < mbrUnsignedBytes.length; i++ )
                mbrUnsignedBytes[i] = imageStream.read( );
        } catch( IOException ioe ) {
            System.out.println( "Problem: " + ioe );
        }

        initialize( mbrUnsignedBytes );
    }

    /**
     * constructor for MasterBootRecord
     * 
     * @param mbrUnsignedBytes
     *            a boot record as bytes
     */
    public MasterBootRecord( int[] mbrUnsignedBytes ) {

        initialize( mbrUnsignedBytes );
    }

    /**
     * initializes datamembers from bytes representing an MBR
     * 
     * @param mbrUnsignedBytes
     *            a boot record as bytes
     */
    private void initialize( int[] mbrUnsignedBytes ) {

        this.mbrUnsignedBytes = mbrUnsignedBytes;
        // carve out CODE_SIZE bytes from image
        bootCode = new int[ BOOT_CODE_SIZE ];
        for( int i = 0; i < BOOT_CODE_SIZE; i++ )
            bootCode[i] = mbrUnsignedBytes[i];

        // carve out Partition Entries from image
        int[] pe1 = new int[ 16 ];
        int[] pe2 = new int[ 16 ];
        int[] pe3 = new int[ 16 ];
        int[] pe4 = new int[ 16 ];

        for( int i = BOOT_CODE_SIZE; i < BOOT_CODE_SIZE + 16; i++ ) // loop
        // through 4
        // partition
        // tables
        // (16 bytes
        // each)
        {
            pe1[i - BOOT_CODE_SIZE] = mbrUnsignedBytes[i]; // copy byte from mbr
            // to partition entry
            // buffer 1
            pe2[i - BOOT_CODE_SIZE] = mbrUnsignedBytes[i + 16]; // copy byte
            // from mbr to
            // partition
            // entry buffer
            // 2
            pe3[i - BOOT_CODE_SIZE] = mbrUnsignedBytes[i + 32]; // copy byte
            // from mbr to
            // partition
            // entry buffer
            // 3
            pe4[i - BOOT_CODE_SIZE] = mbrUnsignedBytes[i + 48]; // copy byte
            // from mbr to
            // partition
            // entry buffer
            // 4
        }
        partitionEntry1 = new PartitionEntry( pe1 );
        partitionEntry2 = new PartitionEntry( pe2 );
        partitionEntry3 = new PartitionEntry( pe3 );
        partitionEntry4 = new PartitionEntry( pe4 );

        // carve out marker from image
        marker = new int[ MARKER_SIZE ];

        for( int i = BOOT_CODE_SIZE + 64; i < BOOT_CODE_SIZE + 64 + 2; i++ ) {
            marker[i - BOOT_CODE_SIZE - 64] = mbrUnsignedBytes[i];
        }
    }

    /**
     * inspector for bootCode
     * 
     * @return the bootCode datamember
     */
    public int[] getBootCode( ) {

        return bootCode;
    }

    /**
     * inspector for marker
     * 
     * @return the marker datamember
     */
    public int[] getMarker( ) {

        return marker;
    }

    /**
     * determines a bootable partition
     * 
     * @return the bootable PartitionEntry
     */
    public PartitionEntry getBootablePartition( ) {

        if( partitionEntry1.isBootable( ) )
            return partitionEntry1;
        else if( partitionEntry2.isBootable( ) )
            return partitionEntry2;
        else if( partitionEntry3.isBootable( ) )
            return partitionEntry3;
        else if( partitionEntry4.isBootable( ) )
            return partitionEntry4;
        else
            return null;
    }

    /**
     * determines bootable partition index
     * 
     * @return the id of the bootable partition (1-4)
     */
    public int getBootablePartitionIndex( ) {

        if( partitionEntry1.isBootable( ) )
            return 1;
        else if( partitionEntry2.isBootable( ) )
            return 2;
        else if( partitionEntry3.isBootable( ) )
            return 3;
        else if( partitionEntry4.isBootable( ) )
            return 4;
        else
            return -1;
    }

    /**
     * inspector for ParitionEntry1
     * 
     * @return the PartitionEntry1 datamember
     */
    public PartitionEntry getPartitionEntry1( ) {

        return partitionEntry1;
    }

    /**
     * inspector for ParitionEntry2
     * 
     * @return the PartitionEntry2 datamember
     */
    public PartitionEntry getPartitionEntry2( ) {

        return partitionEntry2;
    }

    /**
     * inspector for ParitionEntry3
     * 
     * @return the PartitionEntry3 datamember
     */
    public PartitionEntry getPartitionEntry3( ) {

        return partitionEntry3;
    }

    /**
     * inspector for ParitionEntry4
     * 
     * @return the PartitionEntry4 datamember
     */
    public PartitionEntry getPartitionEntry4( ) {

        return partitionEntry4;
    }

    /**
     * get total sectors reported by all partitions on disk
     * 
     * Note on some drives the Vista installer may actually set this value too
     * large It has been observed on 13.5 GB drives
     * 
     * @return the total number of sectors
     */
    public long totalSectorsFromPartitions( ) {

        return partitionEntry1.getNumSectors( )
                + partitionEntry2.getNumSectors( )
                + partitionEntry3.getNumSectors( )
                + partitionEntry4.getNumSectors( );
    }

    /**
     * use disk image file size to calculate total sectors
     * 
     * @return total number of sectors
     */
    public long totalSectorsOnDiskFromFile( ) {

        return getFileSizeBytes( ) / BYTES_PER_SECTOR;
    }

    /**
     * used for getting the "end cylinder" value for a disk
     * 
     * @return the end cylinder
     */
    public long largestCylinderValOnDisk( ) {

        // return the maximum end cylinder value for the four partitions
        return Math.max( Math.max( partitionEntry1.getEndCylinder( ),
                partitionEntry2.getEndCylinder( ) ), Math.max( partitionEntry3
                .getEndCylinder( ), partitionEntry4.getEndCylinder( ) ) );
    }

    /**
     * Checks if the MBR is valid by checking if there is a bootable partition
     * and if the bootable partition is valid (of a known type)
     * 
     * @return true if there is a bootable partition, false otherwise
     */
    public boolean isValidMBR( ) {

        if( getBootablePartition( ) == null ) // if there is no bootable
            // partition (0x80 flag)
            return false;

        if( !getBootablePartition( ).isValidPartition( ) ) // if bootable is not
            // a valid partition
            return false;

        return true;
    }

    /**
     * Checks for the 33 C0 8E starting bootcode common to known Windows Systems
     * 
     * @return true - appears to be a windows bootcode, false otherwise
     */
    public boolean hasWindowsBootcode( ) {

        return( bootCode[0] == 0x33 && bootCode[1] == 0xC0 && bootCode[2] == 0x8E );
    }

    /**
     * generic toString method that assembles datamembers
     * 
     * @return a formated string
     */
    public String toString( ) {

        StringBuffer sb = new StringBuffer( );
        for( int i = 0; i < mbrUnsignedBytes.length; i++ ) {
            if( i % 16 == 0 )
                sb.append( System.getProperty( "line.separator" ) );
            if( mbrUnsignedBytes[i] < 16 )
                sb.append( "0" + Integer.toHexString( mbrUnsignedBytes[i] )
                        + " " ); // pad 0-F with leading 0
            else
                sb.append( Integer.toHexString( mbrUnsignedBytes[i] ) + " " ); // print
            // two
            // char
            // hex
            // val
            // 0-255
        }
        sb.append( System.getProperty( "line.separator" ) );
        sb.append( "Partition 1:" + System.getProperty( "line.separator" ) );
        sb.append( "===================="
                + System.getProperty( "line.separator" ) );
        sb.append( partitionEntry1.toString( ) );
        sb.append( "Partition 2:" + System.getProperty( "line.separator" ) );
        sb.append( "===================="
                + System.getProperty( "line.separator" ) );
        sb.append( partitionEntry2.toString( ) );
        sb.append( "Partition 3:" + System.getProperty( "line.separator" ) );
        sb.append( "===================="
                + System.getProperty( "line.separator" ) );
        sb.append( partitionEntry3.toString( ) );
        sb.append( "Partition 4:" + System.getProperty( "line.separator" ) );
        sb.append( "===================="
                + System.getProperty( "line.separator" ) );
        sb.append( partitionEntry4.toString( ) );
        sb.append( System.getProperty( "line.separator" )
                + System.getProperty( "line.separator" ) );

        sb.append( "Total sectors: " + totalSectorsOnDiskFromFile( )
                + System.getProperty( "line.separator" ) );
        sb.append( "Total cylinders: " + largestCylinderValOnDisk( ) );

        return sb.toString( );
    }

    /**
     * @param fileSizeBytes
     *            the fileSizeBytes to set
     */
    public void setFileSizeBytes( long fileSizeBytes ) {

        this.fileSizeBytes = fileSizeBytes;
    }

    /**
     * @return the fileSizeBytes
     */
    public long getFileSizeBytes( ) {

        return fileSizeBytes;
    }
}
