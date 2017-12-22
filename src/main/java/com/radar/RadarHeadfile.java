package com.radar;

import java.util.Map;

public class RadarHeadfile {
    private Map<String,String> dimention;
    private Map<String,String> variable;
    private Map<String,String> attribute;

    public void setDimention(Map dimention) {
        this.dimention = dimention;
    }

    public void setVariable(Map variable) {
        this.variable = variable;
    }

    public void setAttribute(Map attribute) {
        this.attribute = attribute;
    }

    public Map<String, String> getDimention() {
        return dimention;
    }

    public Map<String, String> getAttribute() {
        return attribute;
    }

    public Map<String, String> getVariable() {
        return variable;
    }
}
