package com.error;

import java.util.Random;

public abstract class CodeBase {
    protected int[] data;
    protected int[] code;
    protected int[] type;
    protected int errors = 0;

    public void setData(String str) {
        int n = str.length();
        data = new int[n];
        for (int i = 0; i < n; i++) {
            if (str.charAt(i) == '1') data[i] = 1;
            else data[i] = 0;
        }
        errors = 0;
    }

    public void setCode(String str) {
        int n = str.length();
        code = new int[n];
        this.type = new int[n];
        for (int i = 0; i < n; i++) {
            if (str.charAt(i) == '1') code[i] = 1;
            else code[i] = 0;
        }
        errors = 0;
    }

    public abstract void encode();

    public abstract void decode();

    public int getDataBitsNumber() {
        return data.length;
    }

    public int getControlBitsNumber() {
        return code.length - data.length;
    }

    public int getDetectedErrorsNumber() {
        return errors;
    }

    public int getFixedErrorsNumber() {
        int b = 0;
        for (int value : type) {
            if (value == 1 || value == 4) b++;
        }
        return b;
    }

    public String codeToString() {
        DataBits d = new DataBits();
        d.fromInt(code);
        return d.toString();
    }

    public String dataToString() {
        DataBits d = new DataBits();
        d.fromInt(data);
        return d.toString();
    }

    public int[] getBitTypes() {
        return type;
    }

    public abstract void fix();

    public void interfere(int n) {
        int l = code.length;
        Random generator = new Random();
        if (n > l) n = l;
        int pozycja;
        int zaklocone = 0;
        for (int i = 0; i < l; i++) type[i] = 0;
        while (zaklocone < n) {
            pozycja = generator.nextInt(l);
            if (type[pozycja] == 0) {
                if (code[pozycja] == 1) code[pozycja] = 0;
                else code[pozycja] = 1;
                type[pozycja] = 1;
                zaklocone++;
            }
        }
    }

}
