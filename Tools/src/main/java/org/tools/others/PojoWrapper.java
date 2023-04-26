package org.tools.others;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PojoWrapper{
    private Class objectClass;
    private List<Cell> cells;
    private List<String> fields;
    private List<String> headers;

    public PojoWrapper(Class objectClass, List<String> fields, List<String> headers){
        this.objectClass = objectClass;
        this.fields = fields;
        this.headers = headers;
        prepareCells();
    }

    private List<Cell> prepareCells(){
        cells = new ArrayList<Cell>();
        boolean allFields = fields==null || fields.isEmpty();
        if(allFields){
            fields = new ArrayList<String>();
            headers = new ArrayList<String>();
            Field[] classFields = objectClass.getDeclaredFields();
            for(Field field : classFields){
                String fieldName = field.getName();
                String fieldLabel = fieldName.toUpperCase();
                Cell fieldCell = new Cell(fieldName, fieldLabel);
                fieldCell.setField(field);
                cells.add(fieldCell);
                fields.add(fieldName);
                headers.add(fieldLabel);
            }
        }else{
            boolean useHeaders = headers!=null && !headers.isEmpty();
            if(!useHeaders)
                headers = new ArrayList<String>();
            for(int i=0; i<fields.size(); i++){
                String fieldName = fields.get(i);
                String fieldLabel = fieldName.toUpperCase();
                if(useHeaders && i<headers.size())
                    fieldLabel = headers.get(i);
                else
                    headers.add(fieldLabel);

                Cell fieldCell = new Cell(fieldName, fieldLabel);
                Field classField = null;
                try {
                    classField = objectClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                fieldCell.setField(classField);
                cells.add(fieldCell);
            }
        }
        return cells;
    }

    private List<Cell> prepareValueSuppliers(Object instance, Object defaultValue){
        final Object localDefaultValue = defaultValue!=null ? defaultValue : "";
        for(Cell cell : cells){
            Field field = cell.getField();
            Supplier<Object> valueSupplier = ()-> localDefaultValue;
            if(field!=null){
                try{
                    field.setAccessible(true);
                    valueSupplier = ()-> {
                        try {
                            return field.get(instance);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                            return localDefaultValue;
                        }
                    };
                }catch(Exception e){
                    e.printStackTrace();
                    valueSupplier = ()-> localDefaultValue;
                }
            }
            cell.setValueSupplier(valueSupplier);
        }
        return cells;
    }

    public List<Cell> getCells(Object instance, Object defaultValue) throws Exception{
        if(instance==null || !instance.getClass().equals(objectClass)){
            throw new Exception("Objet null ou n'est pas une instance de la class: "+objectClass.getCanonicalName());
        }
        prepareValueSuppliers(instance, defaultValue);
        return cells;
    }


    /*----------------Setters and getters ------------------*/
    public Class getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(Class objectClass) {
        this.objectClass = objectClass;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
}

