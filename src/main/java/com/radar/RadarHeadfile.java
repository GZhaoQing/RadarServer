package com.radar;

import java.util.Map;

public class RadarHeadfile {
    private Map<String,String> dimention;
    private Map<String,Variable4Json> variable;
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

    public Map<String, Variable4Json> getVariable() {
        return variable;
    }
}
