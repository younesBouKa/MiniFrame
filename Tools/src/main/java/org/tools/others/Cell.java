package org.tools.others;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class Cell{
    private String colName;
    private String colLabel;
    private Supplier<Object> valueSupplier;
    private Field field;
    public Cell(String colName, String colLabel){
        this.colName = colName;
        this.colLabel = colLabel;
    }
    public String getColName() {
        return colName;
    }
    public void setColName(String colName) {
        this.colName = colName;
    }
    public String getColLabel() {
        return colLabel;
    }
    public void setColLabel(String colLabel) {
        this.colLabel = colLabel;
    }
    public Supplier<Object> getValueSupplier() {
        return valueSupplier;
    }
    public void setValueSupplier(Supplier<Object> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }
    public Object getValue(){
        return this.valueSupplier.get();
    }
    public Field getField() {
        return field;
    }
    public void setField(Field field) {
        this.field = field;
    }
}
