package org.injection.enums;

public enum DependencyType {
    FIRST,
    CONSTRUCTOR_ARGUMENT,
    METHOD_ARGUMENT,
    FIELD;

    public String format(Object first,Object second){
        switch (this){
            case FIRST:
                return first.toString();
            case CONSTRUCTOR_ARGUMENT:
                return "["+second.toString()+"] <As argument for constructor of> ["+first.toString()+"]";
            case METHOD_ARGUMENT:
                return "["+second.toString()+"] <As argument for method of> ["+first.toString()+"]";
            case FIELD:
                return "["+second.toString()+"] <As field of> ["+first.toString()+"]";
            default:
                return "["+first.toString()+"] <"+this.name()+"> ["+second.toString()+"]";
        }
    }
}
