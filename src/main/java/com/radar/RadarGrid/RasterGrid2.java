package com.radar.RadarGrid;

//import GeoMeteoro.BasicLib.BasicUtils;
//import GeoMeteoro.GeoData.VectorData.OutBox;
//import GeoMeteoro.GeoData.VectorData.vPoint;
/**
 * Created by Bmoonlu on 2017/7/18.
 */


/*******该类用于二维栅格数据的处理****************************************************/
public abstract class RasterGrid2  extends  RasterGridBase{

    public int m_nRows;
    public int m_nCols;
    public float m_llx;
    public float m_lly;
    public float m_cellSize;




    public void copyFrame_from(RasterGrid2 oriRasterGrid){
        m_llx =oriRasterGrid. m_llx;
        m_lly = oriRasterGrid. m_lly;
        m_cellSize =oriRasterGrid. m_cellSize;
        m_nRows = oriRasterGrid.m_nRows;
        m_nCols = oriRasterGrid.m_nCols;
    }

    public float get_xMax(){
        return (m_llx + m_cellSize * m_nCols);
    }
    public float get_yMax() { return (m_lly + m_cellSize * m_nRows);}


//    public OutBox get_outBox(){
//
//        float xMin = m_llx;
//        float xMax = get_xMax();
//        float yMin = m_lly;
//        float yMax= get_yMax();
//
//        return  new OutBox(xMin, xMax,yMin,yMax );
//
//    }




}

