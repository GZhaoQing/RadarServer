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

import java.io.IOException;
import java.util.*;

public class FileParser {
    private String defaultPath;
    private int[] shape=new int[2];
    private float[] azimuth;
    private int gNum;
    public FileParser() {
    }
    public FileParser(String defaultPath) {
        this.defaultPath = defaultPath;
    }
//    public RadarFile parseWithImg() throws IOException {
//        return parseWithImg(defaultPath);
//    }
//    public RadarFile parseWithImg(String fileIn) throws IOException {
//        return parseWithImg(fileIn,null);
//    }
    public RadarFile readWithImg(String fileIn,String imagePath) throws IOException {
        //取得文件名
        int pos=fileIn.lastIndexOf("/");
        String name=fileIn.substring(pos+1);

        RadarFile radarFile=new RadarFile();
        RadarHeadfile hFile=new RadarHeadfile();
        NetcdfDataset ncds=null;
        try {
            ncds= NetcdfDataset.openDataset(fileIn);
            hFile=readHeadInfo(ncds);
            radarFile.setHeadfile(hFile);

            return setImage(ncds,radarFile,imagePath,name);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(ncds!=null){
                ncds.close();
            }
        }
        return radarFile;
    }
    public RasterGrid2_Byte readGridData(String filePath) throws IOException {
        NetcdfDataset ncds = null;
        FeatureDataset fds=null;
        try{
            ncds=NetcdfDataset.openDataset(filePath);
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
            fds =
                    FeatureDatasetFactoryManager.wrap(
                            FeatureType.ANY,
                            ncds,
                            emptyCancelTask,
                            fm);
            SimpleDataModule rawData=getVarAndAxis(ncds,fds);
            RasterGridBuilder rgBuilder=new RasterGridBuilder();
            RasterGrid2_Byte rg_byte=rgBuilder.build((Variable) rawData.getVar(),rawData.getAxisList());
            return rg_byte;
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            ncds.close();
            fds.close();
        }
        return null;
    }

    private RadarHeadfile readHeadInfo(NetcdfFile ncFile){
        RadarHeadfile hFile=new RadarHeadfile();
        hFile.setDimention(readDimensions(ncFile.getDimensions()));
        hFile.setVariable(readVariables(ncFile.getVariables()));
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
    private LinkedHashMap readVariables(List<Variable> listV){
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
                vj.setMembers(readVariables(struct_vList));
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
    private LinkedHashMap readAttributes(List<Attribute> listA){
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

    private SimpleDataModule getVarAndAxis(NetcdfDataset ncds,FeatureDataset fds) throws IOException {

            FeatureType type=fds.getFeatureType();
            if(type==FeatureType.RADIAL){
                RadialDatasetSweep rds=(RadialDatasetSweep)fds;
                List<VariableSimpleIF> list=
                        rds.getDataVariables();
                int size=list.size();
                String[] dataV=new String[size];
                RadialDatasetSweep.RadialVariable varRaf=null;
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
                varRaf =
                        (RadialDatasetSweep.RadialVariable)
                                rds.getDataVariable(dataV[0]);//暂时只读一个
                return new SimpleDataModule(type,varRaf);
            }else{
                List<CoordinateAxis> axisList = ncds.getCoordinateAxes();
                List<VariableSimpleIF> list=
                        fds.getDataVariables();
                Variable var=null;
                for(VariableSimpleIF v:list){
                    Variable vt=(Variable)v;
                    System.out.println(vt.getDataType());
                    if(vt.getDataType()== DataType.BYTE||vt.getDataType()==DataType.SHORT){
                        var=vt;
                    }
                }
                return new SimpleDataModule(type,var,axisList);
            }


    }

    private float[] readRadialData(SimpleDataModule rawData) throws IOException {
        RadialDatasetSweep.RadialVariable varRef =(RadialDatasetSweep.RadialVariable) rawData.getVar();
        float[] data = varRef.readAllData();

        RadialDatasetSweep.Sweep sweep=varRef.getSweep(0);
        azimuth=sweep.getAzimuth();
        gNum=sweep.getGateNumber();
        return data;
    }
    private int[] readFeatureData(SimpleDataModule rawData) throws IOException {
        Variable var=(Variable) rawData.getVar();
        List<CoordinateAxis> axisList=rawData.getAxisList();

        Iterator it=var.getDimensions().iterator();
        Dimension d=null;
        while(it.hasNext()){
            d=(Dimension) it.next();
            for(CoordinateAxis axis:axisList){
                if(d.getFullName().equals(axis.getFullName())){
                    AxisType type=axis.getAxisType();
                    if(type==AxisType.GeoX){
                        shape[0]=d.getLength();
                    }else if(type==AxisType.GeoY){
                        shape[1]=d.getLength();
                    }
                    break;
                }
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


    private RadarFile setImage(NetcdfDataset ncds,RadarFile radarFile,String imagePath,String name) throws IOException {
        FeatureDataset fds=null;
        try{
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
            fds =
                    FeatureDatasetFactoryManager.wrap(
                            FeatureType.ANY,
                            ncds,
                            emptyCancelTask,
                            fm);

            SimpleDataModule rawData=getVarAndAxis(ncds,fds);

            ImageCreator ic=new ImageCreator(imagePath);
            FeatureType type=rawData.type;
            if(type==FeatureType.RADIAL){
                radarFile.setImgUrl(ic.createImage(
                        ImageCreator.RGB_RADIAL,
                        readRadialData(rawData),
                        azimuth,
                        gNum,
                        name)
                );
            }else{
                if(ncds.getFileTypeId().equals("NIDS")){//Grid格式NEXRAD雷达数据
                    radarFile.setImgUrl(ic.createImage(
                            ImageCreator.RGB_GRID,
                            readFeatureData(rawData),
                            shape[0],
                            shape[1],
                            name)
                    );
                }else{
                    radarFile.setImgUrl(ic.createImage(
                            ImageCreator.GRAY,
                            readFeatureData(rawData),
                            shape[0],
                            shape[1],
                            name)
                    );
                }
            }
            return radarFile;
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            fds.close();
        }
        return null;
    }
    private class SimpleDataModule {
        private FeatureType type;
        private VariableSimpleIF var;
        private List<CoordinateAxis> axisList;

        public SimpleDataModule(FeatureType type,VariableSimpleIF var) {
            this.var = var;
        }

        public SimpleDataModule(FeatureType type,VariableSimpleIF var, List<CoordinateAxis> axisList) {
            this.var = var;
            this.axisList = axisList;
        }

        public VariableSimpleIF getVar() {
            return var;
        }

        public List<CoordinateAxis> getAxisList() {
            return axisList;
        }

        public FeatureType getType() {
            return type;
        }
    }
}
