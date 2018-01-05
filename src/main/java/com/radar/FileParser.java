package com.radar;

import com.radar.RadarGrid.RasterGrid2_Byte;
import com.radar.RadarGrid.RasterGridBuilder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileParser {
    private String defaultPath=Thread.currentThread().getContextClassLoader().getResource("" ).toString();
    public RasterGrid2_Byte readFile(String filePath){
        NetcdfDataset ncds = null;
        try{
            ncds=NetcdfDataset.openDataset(filePath);

            RasterGridBuilder rgBuilder=new RasterGridBuilder();
            RasterGrid2_Byte rg_byte=rgBuilder.build(ncds);
            return rg_byte;
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try{
                if(ncds!=null){
                    ncds.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }
    private NetcdfDataset ncds;
    private int[] shape=new int[2];
    public RadarFile parse(String fileIn,String name) throws IOException {
        RadarFile radarFile=new RadarFile();
        RadarHeadfile hFile=new RadarHeadfile();

        try {
            ncds= NetcdfDataset.openDataset(fileIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        hFile=readHeadInfo(ncds);
        radarFile.setHeadfile(hFile);
        //判断数据类型
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
        FeatureDataset fds =
                FeatureDatasetFactoryManager.open(
                        FeatureType.ANY,
                        fileIn,
                        emptyCancelTask,
                        fm);
        FeatureType type=fds.getFeatureType();
        if(type==FeatureType.RADIAL){
            readRadialData(fds);
        }else{
            if(ncds.getFileTypeId().equals("NIDS")){
                radarFile.setImgUrl(writeRGBFile(readFeatureData(fds),shape[0],shape[1],name));
            }else{
                radarFile.setImgUrl(writePixFile(readFeatureData(fds),shape[0],shape[1],name));
            }
        }
//        radarFile.setImgUrl(wirteRGBFile(readData(fileIn),fileIndefault));
        return radarFile;
    }
    public RadarHeadfile readHeadInfo(NetcdfFile ncFile){
        RadarHeadfile hFile=new RadarHeadfile();
        hFile.setDimention(readDimensions(ncFile.getDimensions()));
        hFile.setVariable(readVairables(ncFile.getVariables()));
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
    private Map readVairables(List<Variable> listV){
        LinkedHashMap linkMap=new LinkedHashMap();
        Variable var=null;
        StringBuilder str=new StringBuilder();
        StringBuilder strType = new StringBuilder();
        StringBuilder strAtt=new StringBuilder();
        Iterator vit=listV.iterator();
        while (vit.hasNext()){
            var=(Variable)vit.next();
            Variable4Json vj=new Variable4Json();
            DataType dataType=var.getDataType();

            if (dataType == null) {
                strType.append("Unknown");
            } else if (dataType.isEnum()) {
                if (var.getEnumTypedef() == null) {
                    strType.append("enum UNKNOWN");
                } else {
                    strType.append("enum "+NetcdfFile.makeValidCDLName(var.getEnumTypedef().getShortName()));
                }
            } else if(dataType==DataType.STRUCTURE){
                strType.append(dataType);
                Structure struct=(Structure) var;
                List<Variable> struct_vList=struct.getVariables();
                vj.setMembers(readVairables(struct_vList));
            } else {
                strType.append(dataType);
            }

//            str.append(" ");
            Formatter buf=new Formatter();
            var.getNameAndDimensions(buf, false, false);
            str.append(buf.toString());

            Iterator attrs = var.getAttributes().iterator();
            while( attrs.hasNext()) {
                Attribute att = (Attribute)attrs.next();
                if(!att.toString().equals("")){
                    strAtt.append(":");
                    strAtt.append(readOneAttr(att));
                    strAtt.append(";");
                    if (att.getDataType() != DataType.STRING) {
                        strAtt.append(" //"+att.getDataType());
                    }
                }

            }
            vj.setType(dataType.toString());
            vj.setAttribute(strAtt.toString());
            linkMap.put(str.toString(),vj);
            str.delete(0,str.length());
            strType.delete(0,strType.length());
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

    private float[] readRadialData(FeatureDataset fds) throws IOException {
        RadialDatasetSweep rds=(RadialDatasetSweep) fds;
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
        shape=ncds.findVariable(dataV[0]).getShape();
        return rData;
    }
    private int[] readFeatureData(FeatureDataset fds) throws IOException {
        List<VariableSimpleIF> list=
                fds.getDataVariables();
        Variable var=(Variable) list.iterator().next();

        Iterator it=var.getDimensions().iterator();
        Dimension d=null;
        while(it.hasNext()){
            d=(Dimension) it.next();
            CoordinateAxis axis=ncds.findCoordinateAxis(d.getFullName());
            AxisType type=axis.getAxisType();
            if(type==AxisType.GeoX){
                shape[0]=d.getLength();
            }else if(type==AxisType.GeoY){
                shape[1]=d.getLength();
            }
        }
        Array array=var.read();
        int length=(int)array.getSize();
        int[] data=new int[length];
        for(int i=0;i<length;i++){
            data[i]=array.getInt(i);
        }
        return data;
    }

        private String imagePath;
        public String writeRGBFile(int[] data,int width,int height,String fileName) throws IOException{
            String filePath;
            if(imagePath!=null){
                filePath=imagePath;
            }else{
//           filePath="file:/E:/win7sp1/apache-tomcat-8.0.38-windows-x64/apache-tomcat-8.0.38/webapps/radar/img/";
                filePath="";
            }

            String imgPath=fileName+".jpg";

            BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);

            for(int i=0 ;i<height;i++){
                for(int j = 0 ;j<width;j++){
                    //按列读取
                    switch (data[j*width+i]){
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
        public String writePixFile(int[] data,int width,int height,String fileName) throws IOException {
            String filePath;
            if(imagePath!=null){
                filePath=imagePath;
            }else{
//           filePath="file:/E:/win7sp1/apache-tomcat-8.0.38-windows-x64/apache-tomcat-8.0.38/webapps/radar/img/";
                filePath="";
            }

            String imgPath=fileName+".jpg";
            BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);

            bi.getRaster().setPixels(0,0,width,height,data);

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
