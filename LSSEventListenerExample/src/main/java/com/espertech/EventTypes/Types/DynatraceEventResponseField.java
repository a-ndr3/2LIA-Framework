package com.espertech.EventTypes.Types;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class DynatraceEventResponseField {
    class Package {
        public int id;
        public String name;
        public double price;
        public List<String> support;
    }

    class Product {
        public int id;
        public String name;
        public double ppt;
        public String currency;
    }

    class PlatformData {
        public String platform;
        public String quoteFor;
        public List<Package> packages;
        public List<Product> products;
    }

    class PackageSupport {
        public int id;
        public String name;
        public BigDecimal price;
        public List<String> support;
    }

    public static PlatformData parsePlatformData(String json){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readerFor(PlatformData.class).readValue(json);
        } catch (IOException e) {
            return null;
        }
    }

    public static PackageSupport parsePackageSupport(String json){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readerFor(PackageSupport.class).readValue(json);
        } catch (IOException e) {
            return null;
        }
    }
}
