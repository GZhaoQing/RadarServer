package com.radar.radargrid;

//import GeoMeteoro.BasicLib.BasicUtils;
//import GeoMeteoro.GeoData.VectorData.PntGrid2;
//import GeoMeteoro.GeoData.VectorData.vPoint;
//import org.apache.commons.lang.ObjectUtils;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.List;

/**
 * Created by Administrator on 2017/7/24.
 */
public class RasterGrid2_Byte extends RasterGrid2 {
//   private  Byte m_NoData = -127;
    public byte[][] m_data;
    public byte m_NoData = 0;

    public RasterGrid2_Byte(float llx, float lly, float cellSize, int nRows, int nCols, byte[][] curData){
        m_llx = llx;
        m_lly = lly;
        m_cellSize = cellSize;
        m_nRows = nRows;
        m_nCols = nCols;
        m_data = curData ;
    }



   public byte get_value(int rowIndex, int colIndex)
    {
        return m_data[rowIndex][colIndex];
    }



   public void set_value(int iRow, int jCol,byte curData){
        m_data[iRow][jCol] = curData;
    }

    //判断某行是否为空；
    public boolean isRow_empty(int rowIndex, byte shredhold){
        for (int j = 0 ; j< m_nCols ; j++){
            if (m_data[rowIndex][j] >= shredhold ) return false;
        }
        return true;
    }

    //判断某列是否为空；
    public boolean isCol_empty(int rolIndex, byte shredhold){
        for (int j = 0  ; j< m_nRows ; j++){
            if (m_data[j][rolIndex] >= shredhold ) return false;
        }
        return true;
    }





}


