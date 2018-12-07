package com.conversion;

public class Main {


    public static void main(String[] args) throws Exception {
        IConverter converter = new RepeaterBookConverter();
        converter.process();
    }



}
