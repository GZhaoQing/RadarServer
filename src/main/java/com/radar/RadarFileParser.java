package com.radar;

import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
/*将文件中的头文件信息和数据图像封装到对象*/
public class RadarFileParser {
    private String defaultPath=Thread.currentThread().getContextClassLoader().getResource("" ).toString();
    private String fileIndefault = "KFWD_SDUS64_NCZGRK_201208150217";
    private String imagePath;
    private NetcdfFile ncFile;
    private int[] shape;
    public RadarFile parse(String fileIn) throws IOException {
        RadarFile radarFile=new RadarFile();
        RadarHeadfile hFile=new RadarHeadfile();

        try {
            ncFile=NetcdfFile.open(fileIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        hFile.setDimention(readDimensions(ncFile.getDimensions()));
        hFile.setVariable(readVairable(ncFile.getVariables()));
        hFile.setAttribute(readAttributes(ncFile.getGlobalAttributes()));
        radarFile.setHeadfile(hFile);


        radarFile.setImgUrl(wirteRGBFile(readData(fileIn),fileIndefault));
        return radarFile;
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
    private Map readVairable(List<Variable> listV){
        LinkedHashMap<String,String>  linkMap=new LinkedHashMap<String, String>();
        Variable var=null;
        StringBuilder str=new StringBuilder();
        StringBuilder strAtt=new StringBuilder();
        Iterator vit=listV.iterator();
        while (vit.hasNext()){
            var=(Variable)vit.next();
            DataType dataType=var.getDataType();

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
                strAtt.append(":");
                strAtt.append(readOneAttr(att));
                strAtt.append(";");
                if (att.getDataType() != DataType.STRING) {
                    strAtt.append(" //"+att.getDataType());
                }
            }
            linkMap.put(str.toString(),strAtt.toString());
            str.delete(0,str.length());
            strAtt.delete(0,strAtt.length());
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

    private float[] readData(String fileIn) throws IOException {
        CancelTask emptyCancelTask = new CancelTask() {
            public boolean isCancel() {
                return false;
            }
            public void setError(String arg0) {
            }

            public void setProgress(String s, int i) {

            }
        };
        Formatter fm=new Formatter();
        RadialDatasetSweep rds = (RadialDatasetSweep)
                FeatureDatasetFactoryManager.open(
                        FeatureType.RADIAL,
                        fileIn,
                        emptyCancelTask,
                        fm);

        List<VariableSimpleIF> list=
                rds.getDataVariables();
        int size=list.size();
        String[] dataV=new String[size];
        RadialDatasetSweep.RadialVariable varRef=null;
        if(size==0){
            dataV[0]=list.get(0).toString();
        }else{
            Iterator it=list.iterator();
            int index=0;
            while(it.hasNext()){
                dataV[index]=it.next().toString();
                index++;
            }
        }

        varRef =
                (RadialDatasetSweep.RadialVariable)
                        rds.getDataVariable(dataV[0]);//暂时只读一个
        float[] rData = varRef.readAllData();
        //数据形状
        shape=ncFile.findVariable(dataV[0]).getShape();
        return rData;
    }
    private String wirteRGBFile(float[] data,String fileIn) throws IOException{
        String filePath;
        if(imagePath!=null){
            filePath=imagePath;
        }else{
//           filePath="file:/E:/win7sp1/apache-tomcat-8.0.38-windows-x64/apache-tomcat-8.0.38/webapps/radar/img/";
            filePath="";
        }

        String imgPath="imgRadar.jpg";
        int width=shape[0];
        int height=shape[1];
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);

        for(int i=0 ;i<width;i++){
            for(int j = 0 ;j<height;j++){
                switch ((int)data[j*232+i]/5){
                    case 0:bi.setRGB(i,j,0xFAFAFA);break;
                    case 1:bi.setRGB(i,j,0xBFBFFC);break;
                    case 2:bi.setRGB(i,j,0x7870ED);break;
                    case 3:bi.setRGB(i,j,0x1C70CF);break;
                    case 4:bi.setRGB(i,j,0xA6FAA6);break;
                    case 5:bi.setRGB(i,j,0x00E800);break;
                    case 6:bi.setRGB(i,j,0x0F911A);break;
                    case 7:bi.setRGB(i,j,0xFAF263);break;
                    case 8:bi.setRGB(i,j,0xC7C712);break;
                    case 9:bi.setRGB(i,j,0x898900);break;
                    case 10:bi.setRGB(i,j,0xFCABAB);break;
                    case 11:bi.setRGB(i,j,0xFC5C5C);break;
                    case 12:bi.setRGB(i,j,0xED122E);break;
                    case 13:bi.setRGB(i,j,0xD48AFF);break;
                    case 14:bi.setRGB(i,j,0xA524FA);break;
                    default:bi.setRGB(i,j,0x000000);break;
                }
            }
        }

        File file=new File(filePath+imgPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        ImageIO.write(bi, "jpg", file);
        return imgPath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
