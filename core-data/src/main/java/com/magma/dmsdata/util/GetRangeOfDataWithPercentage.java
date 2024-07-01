package com.magma.dmsdata.util;
//import jdk.incubator.foreign.SymbolLookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GetRangeOfDataWithPercentage {
    public double propertyMin = 20;
    public double propertyMax = 40;

    public GetRangeOfDataWithPercentage(double propertyMin, double propertyMax) {
        this.propertyMin = propertyMin;
        this.propertyMax = propertyMax;
    }

    public double calculateMean(List<Double> data) {
        double sum;
        int n = data.size();

        sum = 0;
        for (int i = 0; i < n; i++) {
            sum += data.get(i);
        }
        return sum / (double) n;
    }

    public double findMode(List<Double> data) {
        int n = data.size();
        double maxValue = 0;
        double maxCount = 0;
        int i, j;

        for (i = 0; i < n; ++i) {
            int count = 0;
            for (j = 0; j < n; ++j) {
                if (Objects.equals(data.get(j), data.get(i))) {
                    count = count + 1;
                }
            }

            if (count > maxCount) {
                maxCount = count;
                maxValue = data.get(i);
            }
        }
        return maxValue;
    }

    public double calculateSD(List<Double> data) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = data.size();

        for (double num : data) {
            sum += num;
        }

        double mean = sum / length;

        for (double num : data) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    public List<Double> cleanData(List<Double> data) {
        List<Double> cleanedData = data.stream().filter(
                        value -> (Double.compare(this.propertyMax, value) > 0) && (Double.compare(this.propertyMin, value) < 0))
                .collect(Collectors.toList());
        return cleanedData;

    }

    public double checkSkewness(double mean, double mode) {
        if (mean > mode) {
            return 1;

        } else if (mean == mode) {
            return 0;
        } else {
            return -1;
        }

    }

    public HashMap<String, String> calculateRangeWithPercentage(double percentage, List<Double> data) {
        data = cleanData(data);
        double mean = calculateMean(data);
        double std = calculateSD(data);
        double mode = findMode(data);
        double requiredPercentage = percentage;
        double currentPercentage = 0;
        double skewness = checkSkewness(mean, mode);
        double min = mode - 0.5 * std;
        min = mode - skewness * std / (mean - mode);
        double max = 0;
        double finalMin = min;
        List<Double> InRangeData = data.stream().filter(value -> Double.compare(finalMin, value) < 0)
                .collect(Collectors.toList());

        double i = 0;

        while (requiredPercentage > currentPercentage) {
            max = mode + i * skewness * std;
            // double finalMax = max;
            double finalMax = max;
            // InRangeData=InRangeData.stream().filter(value->Double.compare(value,
            // mode+i*std) <0).collect(Collectors.toList());
            List<Double> filteredData = new ArrayList<>();
            for (int j = 0; j < InRangeData.size(); j++) {
                if ((Double.compare(InRangeData.get(j), mode + i * skewness * std) < 0)) {
                    filteredData.add(InRangeData.get(j));
                }
            }

            int size = data.size();
            int rangeSize = filteredData.size();
            currentPercentage = ((double) rangeSize / (double) size) * 100;
            i = i + 0.05;
        }
        max = mode + i * std;
        HashMap<String, String> stat = new HashMap<>();
        Object obj = new Object();

        stat.put("percentage-range-min", Double.toString(min));
        stat.put("percentage-range-max", Double.toString(max));
        stat.put("mean", Double.toString(mean));
        stat.put("mode", Double.toString(mode));
        stat.put("standardDeviation", Double.toString(std));

        return stat;
    }

}