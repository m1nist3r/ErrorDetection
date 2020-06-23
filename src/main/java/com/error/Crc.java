package com.error;

public class Crc extends CodeBase {
    public final static long ATM = 0x107;
    public final static long CRC12 = 0x180F;
    public final static long CRC16 = 0x18005;
    public final static long CRC16_REVERSE = 0x14003;
    public final static long SDLC = 0x11021;
    public final static long SDLC_REVERSE = 0x10811;
    public final static long CRC32 = 0x104C11DB7L;

    protected long key=0x18005;
    protected int keyLength=16;

    public void setKey(long key)
    {
        this.key=key;

        if (key==ATM) keyLength=8;
        else if (key==CRC12) keyLength=12;
        else if (key==CRC32) keyLength=32;
        else keyLength=16;
    }

    private int xor(int a, int b)
    {
        if (a==b) return 0;
        else return 1;
    }

    private int[] countCrc(int[] bits)
    {
        int n = bits.length;
        int[] crc = new int[keyLength];
        int[] temp = new int[n+keyLength];
        System.arraycopy(bits, 0, temp, keyLength, n);
        int[] tkey = new int[keyLength+1];
        for (int i=0; i<keyLength+1; i++)
        {
            if ((key&(1<<i))==0) tkey[i]=0;
            else tkey[i]=1;
        }

        // liczenie CRC
        for (int start=n+keyLength-1; start>keyLength-1; start--)
        {
            if (temp[start]==1)
            {
                for (int i=0; i<keyLength+1; i++)
                {
                    temp[start-i]=xor(temp[start-i], tkey[keyLength-i]);
                }
            }
        }

        System.arraycopy(temp, 0, crc, 0, keyLength);
        return crc;
    }

    @Override
    public void encode()
    {
        int n = data.length;
        int l = n+keyLength;
        code = new int[l];
        type = new int[l];
        System.arraycopy(data, 0, code, keyLength, n);		// kopiowanie danych (z przesunięciem o 16 bitów)
        int [] crc = countCrc(data);
        System.arraycopy(crc, 0, code, 0, keyLength);
        for (int i=0; i<keyLength; i++) type[i] = 3;
        for (int i=keyLength; i<l; i++) type[i] = 0;

    }

    @Override
    public void decode()
    {
        int l = code.length;
        int n = l-keyLength;
        data = new int[n];
        if (n >= 0) System.arraycopy(code, keyLength, data, 0, n);

    }

    @Override
    public void fix()
    {
        int l = code.length;
        type = new int[l];
        int[] crc = countCrc(code);
        boolean ok = true;
        for (int i = 0; i < keyLength; i++)
            if (crc[i] != 0) {
                ok = false;
                break;
            }
        if (ok)
        {
            for (int i=0; i<keyLength; i++) type[i] = 3;
            for (int i=keyLength; i<l; i++) type[i] = 0;
        }
        else
        {
            errors++;
            for (int i=0; i<keyLength; i++) type[i] = 5;
            for (int i=keyLength; i<l; i++) type[i] = 2;
        }
    }
}
