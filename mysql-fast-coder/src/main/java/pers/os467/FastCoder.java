package pers.os467;

import java.sql.*;
import java.util.*;

public class FastCoder {

    public static void main(String[] args) {

        FastCoder fastCoder = FastCoder.get();

        /**
         *
         * <result column="{c}" property="{p}"></result>
         *
         */
        String output = fastCoder.readTable("s_student_info")
                .codeEachLn("<result column=\"{c}\" property=\"{p}\"></result>")
                .generate();

        System.out.println(output);

    }

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String TYPE_NAME = "TYPE_NAME";

    private static final String REMARKS = "REMARKS";

    private static final String SELECT_COLUMNS = "SELECT "+ COLUMN_NAME +"\n" +
            "FROM INFORMATION_SCHEMA.COLUMNS\n" +
            "WHERE TABLE_NAME = ?;";

    private static Connection connection;

    private static String remarkFlag = "{r}";

    private static String columnFlag = "{c}";

    private static String propertyFlag = "{p}";

    private static String typeFlag = "{t}";

    private static String classFlag = "{class}";

    private static Map<String,String> typeMap = new HashMap<>();

    private StringBuilder code = new StringBuilder();

    private List<String> remarks = new ArrayList<>();

    private List<String> types = new ArrayList<>();

    private List<String> columns = new ArrayList<>();

    private List<String> properties = new ArrayList<>();

    private FastCoder(){

    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/yizhi_db?serverTimezone=GMT&characterEncoding=utf-8", "root", "root");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }



        typeMap.put("BIGINT","Long");
        typeMap.put("BINARY","byte[]");
        typeMap.put("BIT","byte[]");
        typeMap.put("BLOB","String");

        typeMap.put("CHAR","String");

        typeMap.put("DATE","Date");
        typeMap.put("DATETIME","Date");
        typeMap.put("DECIMAL","BigDecimal");
        typeMap.put("DOUBLE","Double");

        typeMap.put("INT","Integer");
        typeMap.put("INTEGER","Integer");


        typeMap.put("LONGTEXT","String");
        typeMap.put("LONGBLOB","byte[]");
        typeMap.put("MEDIUMTEXT","String");
        typeMap.put("MEDIUMBLOB","String");

        typeMap.put("TINYINT","Integer");
        typeMap.put("TINYBLOB","String");
        typeMap.put("TINYTEXT","String");
        typeMap.put("TEXT","String");
        typeMap.put("TIMESTAMP","Date");

        typeMap.put("VARCHAR","String");
        typeMap.put("VARBINARY","byte[]");


    }


    private static List<String> getTableColumns(String tableName){

        List<String> fields = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_COLUMNS);
            preparedStatement.setString(1,tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String column = resultSet.getString(COLUMN_NAME);
                fields.add(column);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return fields;
    }

    private static String toProperty(String column){
        String[] split = column.split("_");
        StringBuilder sb = new StringBuilder();
        if (split.length > 0){
            sb.append(split[0]);
        }else {
            return column;
        }
        for (int i = 1; i < split.length; i++) {
            sb.append(split[i].substring(0,1).toUpperCase());
            sb.append(split[i].substring(1));
        }
        return sb.toString();
    }

    public static void setType(String dbType,String javaType) {
        typeMap.put(dbType,javaType);
    }

    public static void setTypeFlag(String flag) {
        typeFlag = flag;
    }

    public static void setPropertyFlag(String flag) {
        propertyFlag = flag;
    }

    public static void setColumnFlag(String flag) {
        columnFlag = flag;
    }

    public static void setClassFlag(String flag) {
        classFlag = flag;
    }


    public static FastCoder get(){
        return new FastCoder();
    }

    public static void setConnection(Connection connection){
        FastCoder.connection = connection;
    }


    public FastCoder codeEachLn(String template) {
        int i = 0;
        for (String column : this.columns) {
            String p = properties.get(i);
            String replace = template.replace(columnFlag, column)
                    .replace(propertyFlag, p)
                    .replace(classFlag,toSimpleClassName(p))
                    .replace(remarkFlag,remarks.get(i))
                    .replace(typeFlag,typeMap.get(types.get(i)));
            code.append(replace);
            code.append("\n");
            i++;
        }
        return this;
    }

    private String toSimpleClassName(String p) {
        return p.substring(0,1).toUpperCase() + p.substring(1);
    }


    public FastCoder code(String template) {
        code.append(template);
        return this;
    }

    public FastCoder readTable(String tableName) {

        DatabaseMetaData metaData;
        try {
            metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            while (columns.next()) {
                this.columns.add(columns.getString(COLUMN_NAME));
                this.types.add(columns.getString(TYPE_NAME));
                this.remarks.add(columns.getString(REMARKS));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        for (String tableColumn : columns) {
            properties.add(toProperty(tableColumn));
        }
        return this;
    }

    public String generate() {
        return code.toString();
    }

}
