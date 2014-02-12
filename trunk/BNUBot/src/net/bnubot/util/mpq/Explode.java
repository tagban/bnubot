/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util.mpq;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Implementation of the pkware EXPLODE algorithm.
 * Copyright (C) 2008 volcore.net, Volker Schönefeld.  All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Volker Schönefeld
 */
public class Explode
{
    public final static int COMPRESSIONTYPE_BINARY = 0;
    public final static int COMPRESSIONTYPE_ASCII = 1;

    protected final static byte[] lenBits =
    {
        0x03, 0x02, 0x03, 0x03, 0x04, 0x04, 0x04, 0x05,
        0x05, 0x05, 0x05, 0x06, 0x06, 0x06, 0x07, 0x07
    };

    protected final static short[] lenCode =
    {
        0x05, 0x03, 0x01, 0x06, 0x0A, 0x02, 0x0C, 0x14,
        0x04, 0x18, 0x08, 0x30, 0x10, 0x20, 0x40, 0x00
    };

    protected final static byte[] exLenBits =
    {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };

    protected final static short[] lenBase =
    {
        0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007,
        0x0008, 0x000A, 0x000E, 0x0016, 0x0026, 0x0046, 0x0086, 0x0106
    };

    protected final static byte[] distBits =
    {
        0x02, 0x04, 0x04, 0x05, 0x05, 0x05, 0x05, 0x06,
        0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
        0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x07, 0x07,
        0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
        0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
        0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
        0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
        0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08
    };

    protected final static short[] distCode =
    {
        0x03, 0x0D, 0x05, 0x19, 0x09, 0x11, 0x01, 0x3E,
        0x1E, 0x2E, 0x0E, 0x36, 0x16, 0x26, 0x06, 0x3A,
        0x1A, 0x2A, 0x0A, 0x32, 0x12, 0x22, 0x42, 0x02,
        0x7C, 0x3C, 0x5C, 0x1C, 0x6C, 0x2C, 0x4C, 0x0C,
        0x74, 0x34, 0x54, 0x14, 0x64, 0x24, 0x44, 0x04,
        0x78, 0x38, 0x58, 0x18, 0x68, 0x28, 0x48, 0x08,
        0xF0, 0x70, 0xB0, 0x30, 0xD0, 0x50, 0x90, 0x10,
        0xE0, 0x60, 0xA0, 0x20, 0xC0, 0x40, 0x80, 0x00
    };

    protected final static byte[] chBitsAsc =
    {
        0x0B, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x08, 0x07, 0x0C, 0x0C, 0x07, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0D, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x04, 0x0A, 0x08, 0x0C, 0x0A, 0x0C, 0x0A, 0x08,
        0x07, 0x07, 0x08, 0x09, 0x07, 0x06, 0x07, 0x08,
        0x07, 0x06, 0x07, 0x07, 0x07, 0x07, 0x08, 0x07,
        0x07, 0x08, 0x08, 0x0C, 0x0B, 0x07, 0x09, 0x0B,
        0x0C, 0x06, 0x07, 0x06, 0x06, 0x05, 0x07, 0x08,
        0x08, 0x06, 0x0B, 0x09, 0x06, 0x07, 0x06, 0x06,
        0x07, 0x0B, 0x06, 0x06, 0x06, 0x07, 0x09, 0x08,
        0x09, 0x09, 0x0B, 0x08, 0x0B, 0x09, 0x0C, 0x08,
        0x0C, 0x05, 0x06, 0x06, 0x06, 0x05, 0x06, 0x06,
        0x06, 0x05, 0x0B, 0x07, 0x05, 0x06, 0x05, 0x05,
        0x06, 0x0A, 0x05, 0x05, 0x05, 0x05, 0x08, 0x07,
        0x08, 0x08, 0x0A, 0x0B, 0x0B, 0x0C, 0x0C, 0x0C,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
        0x0D, 0x0C, 0x0D, 0x0D, 0x0D, 0x0C, 0x0D, 0x0D,
        0x0D, 0x0C, 0x0D, 0x0D, 0x0D, 0x0D, 0x0C, 0x0D,
        0x0D, 0x0D, 0x0C, 0x0C, 0x0C, 0x0D, 0x0D, 0x0D,
        0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D
    };

    protected final static short[] chCodeAsc =
    {
        0x0490, 0x0FE0, 0x07E0, 0x0BE0, 0x03E0, 0x0DE0, 0x05E0, 0x09E0,
        0x01E0, 0x00B8, 0x0062, 0x0EE0, 0x06E0, 0x0022, 0x0AE0, 0x02E0,
        0x0CE0, 0x04E0, 0x08E0, 0x00E0, 0x0F60, 0x0760, 0x0B60, 0x0360,
        0x0D60, 0x0560, 0x1240, 0x0960, 0x0160, 0x0E60, 0x0660, 0x0A60,
        0x000F, 0x0250, 0x0038, 0x0260, 0x0050, 0x0C60, 0x0390, 0x00D8,
        0x0042, 0x0002, 0x0058, 0x01B0, 0x007C, 0x0029, 0x003C, 0x0098,
        0x005C, 0x0009, 0x001C, 0x006C, 0x002C, 0x004C, 0x0018, 0x000C,
        0x0074, 0x00E8, 0x0068, 0x0460, 0x0090, 0x0034, 0x00B0, 0x0710,
        0x0860, 0x0031, 0x0054, 0x0011, 0x0021, 0x0017, 0x0014, 0x00A8,
        0x0028, 0x0001, 0x0310, 0x0130, 0x003E, 0x0064, 0x001E, 0x002E,
        0x0024, 0x0510, 0x000E, 0x0036, 0x0016, 0x0044, 0x0030, 0x00C8,
        0x01D0, 0x00D0, 0x0110, 0x0048, 0x0610, 0x0150, 0x0060, 0x0088,
        0x0FA0, 0x0007, 0x0026, 0x0006, 0x003A, 0x001B, 0x001A, 0x002A,
        0x000A, 0x000B, 0x0210, 0x0004, 0x0013, 0x0032, 0x0003, 0x001D,
        0x0012, 0x0190, 0x000D, 0x0015, 0x0005, 0x0019, 0x0008, 0x0078,
        0x00F0, 0x0070, 0x0290, 0x0410, 0x0010, 0x07A0, 0x0BA0, 0x03A0,
        0x0240, 0x1C40, 0x0C40, 0x1440, 0x0440, 0x1840, 0x0840, 0x1040,
        0x0040, 0x1F80, 0x0F80, 0x1780, 0x0780, 0x1B80, 0x0B80, 0x1380,
        0x0380, 0x1D80, 0x0D80, 0x1580, 0x0580, 0x1980, 0x0980, 0x1180,
        0x0180, 0x1E80, 0x0E80, 0x1680, 0x0680, 0x1A80, 0x0A80, 0x1280,
        0x0280, 0x1C80, 0x0C80, 0x1480, 0x0480, 0x1880, 0x0880, 0x1080,
        0x0080, 0x1F00, 0x0F00, 0x1700, 0x0700, 0x1B00, 0x0B00, 0x1300,
        0x0DA0, 0x05A0, 0x09A0, 0x01A0, 0x0EA0, 0x06A0, 0x0AA0, 0x02A0,
        0x0CA0, 0x04A0, 0x08A0, 0x00A0, 0x0F20, 0x0720, 0x0B20, 0x0320,
        0x0D20, 0x0520, 0x0920, 0x0120, 0x0E20, 0x0620, 0x0A20, 0x0220,
        0x0C20, 0x0420, 0x0820, 0x0020, 0x0FC0, 0x07C0, 0x0BC0, 0x03C0,
        0x0DC0, 0x05C0, 0x09C0, 0x01C0, 0x0EC0, 0x06C0, 0x0AC0, 0x02C0,
        0x0CC0, 0x04C0, 0x08C0, 0x00C0, 0x0F40, 0x0740, 0x0B40, 0x0340,
        0x0300, 0x0D40, 0x1D00, 0x0D00, 0x1500, 0x0540, 0x0500, 0x1900,
        0x0900, 0x0940, 0x1100, 0x0100, 0x1E00, 0x0E00, 0x0140, 0x1600,
        0x0600, 0x1A00, 0x0E40, 0x0640, 0x0A40, 0x0A00, 0x1200, 0x0200,
        0x1C00, 0x0C00, 0x1400, 0x0400, 0x1800, 0x0800, 0x1000, 0x0000
    };


    protected static class Work
    {
        ByteBuffer      ibuf;
        ByteBuffer      obuf;

        int             compressionType;    // 0 -> binary, 1 -> ascii
        int             dictSizeIndex;      // 4->0x400 5->0x600 6->0x800
        int             dictSizeMask;       // 0x0F->0x400 0x1F->0x800 0x3F->0x1000
        int             bitBuffer;            // input buffer
        int             extraBits;          // extra bits in bitBuff

        //byte[]          outBuff = new byte[ 8192 ];
        byte[]          position1 = new byte[ 256 ];
        byte[]          position2 = new byte[ 256 ];
        byte[]          buf2c34 = new byte[ 256 ];
        byte[]          buf2d34 = new byte[ 256 ];
        byte[]          buf2e34 = new byte[ 80 ];
        byte[]          buf2eb4 = new byte[ 256 ];
        byte[]          chBitsAsc = new byte[ 256 ];
        byte[]          distBits = new byte[ 64 ];
        byte[]          lenBits = new byte[ 16 ];
        byte[]          exLenBits = new byte[ 16 ];
        short[]         lenBase = new short[ 16 ];
    }

    protected final static int wasteBits( Work w, int numBits )
    {
        if( numBits <= w.extraBits )
        {
            w.extraBits -= numBits;
            w.bitBuffer = w.bitBuffer >>> numBits;
            return 0;
        }

        w.bitBuffer = w.bitBuffer >>> w.extraBits;
        if( w.ibuf.hasRemaining( ) == false )
            return 1;
        w.bitBuffer |= (w.ibuf.get( )&0xff) << 8;
        w.bitBuffer = w.bitBuffer >>> ( numBits - w.extraBits );
        w.extraBits = w.extraBits - numBits + 8;
        return 0;
    }

    protected final static int decodeLit( Work w )
    {
        int numBits = 0;
        int value = 0;

        if( (w.bitBuffer & 1)==1 )
        {
            if( wasteBits( w, 1 ) == 1 )
                return 0x306;

            value = w.position2[ w.bitBuffer & 0xff ];

            if( wasteBits( w, w.lenBits[ value ] ) == 1 )
                return 0x306;

            numBits = w.exLenBits[ value ];
            if( numBits != 0 )
            {
                int val2 = w.bitBuffer & (( 1 << numBits ) - 1 );

                if( wasteBits( w, numBits ) == 1 )
                {
                    if( ( value + val2 ) != 0x10e )
                        return 0x306;
                }

                value = w.lenBase[ value ] + val2;
            }

            return value + 0x100;
        }

        if( wasteBits( w, 1 ) == 1 )
            return 0x306;

        if( w.compressionType == COMPRESSIONTYPE_BINARY )
        {
            value = w.bitBuffer & 0xff;

            if( wasteBits( w, 8 ) == 1 )
                return 0x306;

            return value;
        }

        // BLABLA when ascii compression, TODO!
        throw new Error( "OMG!" );

        //return 0;
    }

    protected final static int decodeDist( Work w, int length )
    {
        int pos = w.position1[ w.bitBuffer & 0xff ];
        int nskip = w.distBits[ pos ];

        if( wasteBits( w, nskip ) == 1 )
            return 0;

        if( length == 2 )
        {
            pos = ( pos << 2 ) | ( w.bitBuffer & 0x03 );
            if( wasteBits( w, 2 ) == 1 )
                return 0;
        } else
        {
            pos = ( pos << w.dictSizeIndex ) | ( w.bitBuffer & w.dictSizeMask );
            if( wasteBits( w, w.dictSizeIndex ) == 1 )
                return 0;
        }

        return pos+1;
    }

    protected final static void genDecodeTabs( byte[] bits, short[] code, byte[] buffer )
    {
        int count = bits.length;
        for( int i=count-1; i>=0; i-- )
        {
            int idx1 = code[ i ];
            int idx2 = 1 << bits[ i ];

            do
            {
                buffer[ idx1 ] = (byte) i;
                idx1 += idx2;
            } while( idx1 < 0x100 );
        }
    }


    public final static int explode(ByteBuffer input, ByteBuffer output) throws IOException
    {
        if(input.array().length < 4)
            throw new IOException("Explode - Invalid input: need at least 4 bytes of input data.");

        //logger.trace( HexDump.slicedArrayToHexString( input ) );

        Work w = new Work( );
        w.ibuf = input;
        w.obuf = output;

        w.compressionType = w.ibuf.get( );
        w.dictSizeIndex   = w.ibuf.get( );
        w.bitBuffer       = w.ibuf.get( )&0xff;
        w.extraBits       = 0;

        if( 4 > w.dictSizeIndex  || 6 < w.dictSizeIndex )
            throw new IOException( "Explode - Invalid dictionary size, needs to be inbetween 4 and 6." );

        // create the dictionary mask
        w.dictSizeMask     = 0xffff >>> ( 0x10 - w.dictSizeIndex );

        // FIXME: implement ASCII mode as well
        if( w.compressionType != COMPRESSIONTYPE_BINARY )
            throw new IOException( "Explode - Only binary compression type supported!" );

        // copy the buffers
        System.arraycopy( lenBits, 0, w.lenBits, 0, lenBits.length );
        System.arraycopy( exLenBits, 0, w.exLenBits, 0, exLenBits.length );
        System.arraycopy( lenBase, 0, w.lenBase, 0, lenBase.length );
        System.arraycopy( distBits, 0, w.distBits, 0, distBits.length );
        genDecodeTabs( w.lenBits, lenCode, w.position2 );
        genDecodeTabs( w.distBits, distCode, w.position1 );


        // Main expand routine.
            int result, oneByte;
            oneByte = decodeLit( w );
            result = oneByte;
            while( result < 0x305 )
            {
                if( oneByte >= 0x100 )
                {
                    int copyLength = oneByte - 0xFE;

                    int moveBack = decodeDist( w, copyLength );

                    if( moveBack == 0 )
                        throw new IOException( "Explode - Failed to decodeDist!" );

                    int nowpos = w.obuf.position( );

                    for( int i=0; i<copyLength; ++i )
                    {
                        byte t = w.obuf.get( nowpos - moveBack + i );
                        w.obuf.put( t );
                    }
                } else
                {
                    // single byte, just write.
                    w.obuf.put( (byte) oneByte );
                }

                oneByte = decodeLit( w );
                result = oneByte;
            }

        return w.obuf.position();
    }

    public final static byte[] explode(byte[] input, int off, int len, int out_len) throws IOException {
    	ByteBuffer in = ByteBuffer.wrap(input, off, len);
    	ByteBuffer out = ByteBuffer.allocate(out_len);
    	explode(in, out);
    	return out.array();
    }
}
