package com.radar.RadarGrid;

import com.radar.RadarGrid.RasterGrid2_Byte;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

import java.io.IOException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

/*
*设计思路：接收一个存储着数据源NetcdfDataset对象ncds
*1. 通过FeatureDatasetFactoryManager将ncds读取到FeatureDataset对象fds，对fds操作，获得存储的影像数据(Array)和对应的Dimensions列表;
*2. 回到ncds，通过Dimension名称从ncds获取到CoordinateAxis对象，判断其类型AxisType，筛选两个表示平面的维度（x，y）;
*3. 从CoordinateAxis可以获取坐标数据的最大值和最小值，结合shape信息表示的数据数量，可以计算出RasterGrid2_Byte对象中的栅格单元边长；最小值作为数据起始位置和;
*4. 通过判断出的x,y维度对应的Length，将数据从Array存储到二维数据。
*/
public class RasterGridBuilder {
    public RasterGrid2_Byte build(NetcdfDataset ncds){
        float llx = 0;
        float lly = 0 ;
        float cellSize = 0 ;
        int nRows = 0 ;
        int nCols = 0;
        byte[][] curData = null;
        float maxX = 0;
        float maxY = 0;
        FeatureDataset fds = null;

        Array array=null;
        List<Dimension> dList=null;
        //read data
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
            fds = FeatureDatasetFactoryManager.wrap(
                    FeatureType.ANY,
                    ncds,
                    emptyCancelTask ,
                    fm
            );
            FeatureType type=fds.getFeatureType();
//            System.out.println(type);
            //ncds.getFileTypeId().equals("NIDS"
            if(type==FeatureType.RADIAL){
//                RadialDatasetSweep rds =(RadialDatasetSweep) FeatureDatasetFactoryManager.wrap(
//                        FeatureType.RADIAL,
//                        ncds,
//                        emptyCancelTask ,
//                        fm
//                );
//                RadialDatasetSweep.RadialVariable var=readRadialData(rds);
//                array=Array.factory(var.readAllData());
//                dList=var.getDimensions();
            }else {
                Variable var=reaFeaturedData(fds);
                array =var.read();
                dList=var.getDimensions();
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(fds!=null){
                    fds.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //find dimensions of x y
        Iterator it=dList.iterator();
        Dimension d=null;
        while(it.hasNext()){
            d=(Dimension) it.next();
            CoordinateAxis axis=ncds.findCoordinateAxis(d.getFullName());
            AxisType type=axis.getAxisType();
            if(type==AxisType.GeoX){
                nRows=d.getLength();
                llx=(float) axis.getMinValue();
                maxX=(float) axis.getMaxValue();
            }else if(type==AxisType.GeoY){
                nCols=d.getLength();
                lly=(float) axis.getMinValue();
                maxY=(float) axis.getMaxValue();
            }
        }
        //cell size
        if(nCols==nRows){
            cellSize=(maxX-llx)/nCols;
        }else if((maxX-llx)/nCols==(maxY-lly)/nRows){
            cellSize=(maxX-llx)/nCols;
        }
        //data rebuild
        curData=new byte[nRows][nCols];
        for(int i=0;i<nRows;i++){
            for(int j=0;j<nCols;j++){
                curData[i][j]=array.getByte(i*nRows+j);
            }
        }


        return new RasterGrid2_Byte(llx,lly,cellSize,nRows,nCols,curData);
    }

    private Variable reaFeaturedData(FeatureDataset fds){
        List<VariableSimpleIF> vList=fds.getDataVariables();
        Variable var=null;
        Iterator it=vList.iterator();
        while(it.hasNext()){
            var=(Variable)it.next();
            System.out.println(var.getDataType());
            if(var.getDataType()== DataType.BYTE||var.getDataType()==DataType.SHORT){
                return var;
            }
        }
        return null;
    }
//    private RadialDatasetSweep.RadialVariable readRadialData(RadialDatasetSweep rds){
//        List<VariableSimpleIF>  vList=rds.getDataVariables();
//        RadialDatasetSweep.RadialVariable var=null;
//        if(vList.size() == 1){
//            var=(RadialDatasetSweep.RadialVariable) vList.iterator().next();
//        }
//        return var;
//    }

}
