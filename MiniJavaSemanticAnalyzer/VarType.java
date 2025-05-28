public enum VarType {
    CLASS_INSTANCE,
    INTEGER,
    INTEGER_ARRAY,
    BOOLEAN,
    BOOLEAN_ARRAY;

    public static VarType getType(String type) throws Exception {
        System.out.println("type " + type);
        switch (type) {
            case "int": return INTEGER;
            case "int[]": return INTEGER_ARRAY;
            case "boolean": return BOOLEAN;
            case "boolean[]": return BOOLEAN_ARRAY;
            default: return CLASS_INSTANCE;
        }
    }
}