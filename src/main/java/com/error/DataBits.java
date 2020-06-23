package com.error;

import java.util.Random;

public class DataBits {
    protected int n;
    protected int[] bits;

    public void generate(int n)
    {
        this.n=n;
        bits = new int[n];
        Random generator = new Random();
        for (int i=0; i<n; i++)
        {
            bits[i]=generator.nextInt(2);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for (int i=0; i<n; i++)
        {
            str.append(bits[i]);
        }
        return str.toString();
    }

    public void fromInt(int[] bits)
    {
        this.n=bits.length;
        this.bits = new int[n];
        System.arraycopy(bits, 0, this.bits, 0, bits.length);
    }
}
