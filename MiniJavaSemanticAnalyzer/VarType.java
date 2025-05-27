public enum VarType {
    CLASS_INSTANCE,
    INTEGER,
    INTEGER_ARRAY,
    BOOLEAN,
    BOOLEAN_ARRAY;

    public static VarType getType(String type) throws Exception {
        switch (type) {
            case "int": return INTEGER;
        }
        throw new IllegalArgumentException("Unknown variable type: " + type);
    }
}