package com.error;

public class Parity extends CodeBase {
    @Override
    public void encode() {
        int n = data.length;
        int bajty = n / 8;
        n += bajty;
        code = new int[n];
        type = new int[n];

        int jedynki;
        for (int i = 0; i < bajty; i++) {
            jedynki = 0;
            for (int j = 0; j < 8; j++) {
                code[i * 9 + j + 1] = data[i * 8 + j];        // przepisz dane
                jedynki += data[i * 8 + j];            // zliczaj jedynki
            }
            if (jedynki % 2 == 1) code[i * 9] = 1;            // zapisz bit parzystości
            else code[i * 9] = 0;
        }

    }

    @Override
    public void decode() {
        int n = code.length;
        int bajty = n / 9;
        data = new int[bajty * 8];
        int jedynki;
        errors = 0;
        for (int i = 0; i < bajty; i++) {
            jedynki = 0;
            for (int j = 0; j < 8; j++) {
                data[i * 8 + j] = code[i * 9 + j + 1];        // przepisz dane
                jedynki += code[i * 9 + j + 1];            // zliczaj jedynki
            }
            obliczenie(jedynki, i);
        }

    }

    @Override
    public void fix() {
        int n = code.length;
        type = new int[n];
        int bajty = n / 9;
        errors = 0;

        int jedynki;
        for (int i = 0; i < bajty; i++) {
            jedynki = 0;
            for (int j = 0; j < 8; j++) {
                jedynki += code[i * 9 + j + 1];            // zliczaj jedynki
            }
            obliczenie(jedynki, i);
        }
    }

    private void obliczenie(int jedynki, int i) {
        jedynki += code[i * 9];                // dolicz bit parzystości
        if (jedynki % 2 == 0)                // transmisja poprawna
        {
            type[i * 9] = 3;                // poprawny bit kontrolny
            for (int j = 1; j < 9; j++) type[i * 9 + j] = 0;    // poprawne bity danych
        } else                        // wystąpiły błędy
        {
            errors++;
            type[i * 9] = 5;                // niepewny bit kontrolny
            for (int j = 1; j < 9; j++) type[i * 9 + j] = 2;    // niepewne bity danych
        }
    }
}
