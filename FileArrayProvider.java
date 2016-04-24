package com.kmichaelfox.agents.sl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileArrayProvider {

    public static double[] readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<Double> lines = new ArrayList<Double>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(Double.parseDouble(line));
        }
        bufferedReader.close();
        return toDoubleArray(lines);
        
    }
    static double[] toDoubleArray(List<Double> list){
    	  double[] ret = new double[list.size()];
    	  for(int i = 0;i < ret.length;i++)
    	    ret[i] = list.get(i);
    	  return ret;
    	}
}