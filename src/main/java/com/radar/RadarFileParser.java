package com.radar;

import ucar.ma2.DataType;
import ucar.nc2.*;
import java.io.IOException;
import java.util.*;

public class RadarFileParser {
    String fileIndefault = System.getProperty("user.dir" )+"\\src\\main\\resources\\KFWD_SDUS64_NCZGRK_201208150217";
    public RadarHeadfile parse(String path){
        RadarHeadfile hFile=new RadarHeadfile();
        NetcdfFile ncFile=null;
        try {
            ncFile=NetcdfFile.open(fileIndefault);
        } catch (IOException e) {
            e.printStackTrace();
        }

        hFile.setDimention(readDimensions(ncFile.getDimensions()));
        hFile.setAttribute(readAttributes(ncFile.getGlobalAttributes()));
        return hFile;
    }
    private LinkedHashMap readDimensions(List<Dimension> listD){
        LinkedHashMap<String,String>  linkMap=new LinkedHashMap<String, String>();
        Dimension d=null;
        Iterator it=listD.iterator();
        while(it.hasNext()){
            d=(Dimension)it.next();
            linkMap.put(d.getShortName(),String.valueOf(d.getLength()));
        }
        return linkMap;
    }
    private Map readVairable(List<Attribute> listV){
        LinkedHashMap  linkMap=new LinkedHashMap<String, String>();
        Variable var=null;
        Iterator vit=listV.iterator();
        while (vit.hasNext()){
            var=(Variable)vit.next();
            DataType dataType=var.getDataType();
            StringBuilder str=new StringBuilder();
            if (dataType == null) {
                str.append("Unknown");
            } else if (dataType.isEnum()) {
                if (var.getEnumTypedef() == null) {
                    str.append("enum UNKNOWN");
                } else {
                    str.append("enum "+NetcdfFile.makeValidCDLName(var.getEnumTypedef().getShortName()));
                }
            } else {
                str.append(dataType.toString());
            }

            str.append(" ");
            Formatter buf=new Formatter();
            var.getNameAndDimensions(buf, true, false);
            str.append(buf.toString());
            Iterator attrs = var.getAttributes().iterator();
            while( attrs.hasNext()) {
                Attribute att = (Attribute)attrs.next();
                str.append(":");
                str.append(readOneAttr(att));
                str.append(";");
                if (att.getDataType() != DataType.STRING) {
                    str.append(" //"+att.getDataType());
                }
            }
        }
        return linkMap;
    }
    private Map readAttributes(List<Attribute> listA){
        LinkedHashMap  linkMap=new LinkedHashMap<String, String>();
        Attribute attr=null;
        Iterator it=listA.iterator();
        while(it.hasNext()){
            attr=(Attribute) it.next();
             linkMap.put(attr.getFullName(),readOneAttr(attr));
        }

        return linkMap;
    }
    private String readOneAttr(Attribute attr) {
        StringBuilder str = new StringBuilder();
        if (attr.isString()) {
            for (int i = 0; i < attr.getLength(); ++i) {
                if (i != 0) {
                    str.append(",");
                }
                String val = attr.getStringValue(i);
                if (val != null) {
                    str.append(NCdumpW.encodeString(val));
                }
            }
            return str.toString();
        } else {

            for (int i = 0; i < attr.getLength(); ++i) {
                if (i != 0) {
                    str.append(",");
                }
                Number num = attr.getNumericValue(i);
                str.append(String.valueOf(num));
                DataType dataType = attr.getDataType();
                boolean isUnsigned = attr.isUnsigned();
                if (dataType == DataType.FLOAT) {
                    str.append("f");
                } else if (dataType == DataType.SHORT) {
                    if (isUnsigned) {
                        str.append("US");
                    } else {
                        str.append("S");
                    }
                } else if (dataType == DataType.BYTE) {
                    if (isUnsigned) {
                        str.append("UB");
                    } else {
                        str.append("B");
                    }
                } else if (dataType == DataType.LONG) {
                    if (isUnsigned) {
                        str.append("UL");
                    } else {
                        str.append("L");
                    }
                } else if (dataType == DataType.INT && attr.isUnsigned()) {
                    str.append("U");
                }
            }
            return str.toString();
        }
    }
}
