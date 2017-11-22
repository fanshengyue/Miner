package com.fsy.google.utils;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mr.Zhang on 2017/11/22.
 */

public class JsonUtil {

    /**
     * 将java bean实例对象转换为JSon字符串
     *
     * @return 转换后的字符串
     */
    @SuppressLint("WrongConstant")
    public static String obj2json(Object obj) {
        Class objClazz = obj.getClass();
        //        Log.i("ZF", objClazz.getName());
        if (obj instanceof String) {                                    //字符串类型
            return "\"" + obj.toString() + "\"";
        } else if (obj instanceof Character) {                          //字符型
            return "\"" + String.valueOf((char) obj) + "\"";
        } else if (obj instanceof Byte) {                               //byte型
            return ((byte) obj & 0xff) + "";
        } else if (obj instanceof Short) {                              //short型
            return ((Short) obj) + "";
        } else if (obj instanceof Integer) {                            //整型
            return ((Integer) obj) + "";
        } else if (obj instanceof Long) {                               //长整型
            return ((Long) obj) + "";
        } else if (obj instanceof Date) {                               //日期型
            return date2json((Date) obj);
        } else if (obj instanceof Float) {                              //浮点型
            return (float) obj + "";
        } else if (obj instanceof Double) {                             //双精度浮点型
            return (double) obj + "";
        } else if (obj instanceof Boolean) {                            //布尔型
            return (boolean) obj ? "true" : "false";
        } else if (obj instanceof Set) {                                //Set型
            return set2json((Set) obj);
        } else if (obj instanceof List) {                               //List型
            return list2json((List) obj);
        } else if (obj instanceof Map) {                                //Map型
            return map2json((Map) obj);
        } else {
            StringBuffer sb = new StringBuffer(); //初始化返回字符串
            sb.append("{");
            int count = 0;
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if ((field.getModifiers() & 8) == 8) continue;         //类中的静态常理不进行遍历
                if (count != 0) sb.append(",");
                count++;
                sb.append("\"");
                sb.append(field.getName());
                sb.append("\":");
                try {
                    //                    Log.i("ZF",field.get(obj).getClass().getName() + ":" + field.getModifiers());
                    //判断类中成员变量类型是否为数组类型
                    if (field.getType().isArray()) {
                        Object o = field.get(obj);
                        if (o == null) {
                            sb.append("[]");
                        } else {
                            //将数组类型通过toBoxArray方法进行自动装箱为指定的包装类型,
                            // 然后通过Arrays工具类转化为List去处理
                            sb.append(obj2json(Arrays.asList(
                                    toBoxArray(o,field.getType().getComponentType())
                            )));
                        }
                    } else {
                        //为Float或Double包装类型时 且对象为NULL时会有异常的处理
                        if (field.get(obj) == null && ("java.lang.Double".equals(field.getType()
                                .getName())) ||
                                "java.lang.Float".equals(field.getType().getName())) {
                            sb.append("0");
                        } else {
                            sb.append(obj2json(field.get(obj)));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * 将JSon字符串转换为指定的(字节码文件)包装类
     *
     * @param jsonStr 待转换的JSon字符串
     * @param clazz   待转换的包装类的Class字节码文件
     * @return 返回的包装类的实例对象
     */
    @SuppressLint("WrongConstant")
    public static Object json2obj(String jsonStr, Class clazz) {
        Object rtnObj = null;
        //        Log.i("ZF",clazz.getName());
        try {
            //优先处理布尔型因布尔型不是包装类型,使用newInstance时会抛异常
            if ("boolean".equals(clazz.getName())) {                //boolean
                return "true".equals(jsonStr) ? true : false;
            } else if ("char".equals(clazz.getName()) ||
                    "java.lang.Character".equals(clazz.getName())) {//char
                return removeqm(jsonStr).toCharArray()[0];
            } else if ("byte".equals(clazz.getName()) ||
                    "java.lang.Byte".equals(clazz.getName())) {     //byte
                return (byte) Integer.parseInt(jsonStr);
            } else if ("short".equals(clazz.getName()) ||
                    "java.lang.Short".equals(clazz.getName())) {    //short
                return (Short.parseShort(jsonStr));
            } else if ("float".equals(clazz.getName()) ||
                    "java.lang.Float".equals(clazz.getName())) {    //float
                return Float.parseFloat(jsonStr);
            } else if ("double".equals(clazz.getName()) ||
                    "java.lang.Double".equals(clazz.getName())) {   //double
                return Double.parseDouble(jsonStr);
            } else if ("int".equals(clazz.getName()) ||
                    "java.lang.Integer".equals(clazz.getName())) {  //int
                return Integer.parseInt(jsonStr);
            } else if ("long".equals(clazz.getName()) ||
                    "java.lang.Long".equals(clazz.getName())) {     //Long
                return Long.parseLong(jsonStr);
            }
            rtnObj = clazz.newInstance();                                       //包装类型的处理
            if (rtnObj instanceof String) {                                     //字符型
                return removeqm(jsonStr);
            } else if (rtnObj instanceof Date) {                                //日期型
                return json2date(jsonStr);
            } else {                                                            //实体对象
                JSONObject jsonObject = new JSONObject(jsonStr);
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if ((field.getModifiers() & 8) == 8) continue;         //类中的静态常理不进行遍历
                    //截取对象中成员变量的Json字符串
                    String fieldStr = jsonObject.getString(field.getName());
                    //                    Log.i("ZF", fieldStr + "/" + field.getType().getName());
                    //判断是否为数组类型
                    if (field.getType().isArray()) {
                        List list = json2list(new ArrayList(), fieldStr, field.getType()
                                .getComponentType());
                        //                        field.set(rtnObj, tobytearray(list.toArray()));
                        //通过toAssignArray方法转换为目标字节码文件的数组
                        field.set(rtnObj,toAssignArray(list.toArray(),field.getType().getComponentType()));
                    } else if ("java.util.Map".equals(field.getType().getName()) ||
                            "java.util.HashMap".equals(field.getType().getName()) ||
                            "java.util.LinkedHashMap".equals(field.getType().getName()) ||
                            "java.util.TreeMap".equals(field.getType().getName())) {//判断是否为Map
                        //                        Log.i("ZF", "Here!");
                        Class keyClz = getGenericType(field, 0);
                        Class valClz = getGenericType(field, 1);
                        Map map = (Map) field.get(rtnObj);
                        map = json2map(map, fieldStr, keyClz, valClz);
                    } else if ("java.util.List".equals(field.getType().getName()) ||
                            "java.util.ArrayList".equals(field.getType().getName()) ||
                            "java.util.LinkedList".equals(field.getType().getName())) {
                        Class itmClz = getGenericType(field, 0);
                        List list = (List) field.get(rtnObj);
                        list = json2list(list, fieldStr, itmClz);
                    } else if ("java.util.Set".equals(field.getType().getName()) ||
                            "java.util.HashSet".equals(field.getType().getName()) ||
                            "java.util.LinkedHashSet".equals(field.getType().getName()) ||
                            "java.util.TreeSet".equals(field.getType().getName())) {
                        Class itmClz = getGenericType(field, 0);
                        Set set = (Set) field.get(rtnObj);
                        set = json2set(set, fieldStr, itmClz);
                    } else {
                        field.set(rtnObj, json2obj(fieldStr, field.getType()));
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rtnObj;
    }

    private static Date json2date(String jsonStr) {
        Map<String, Long> mapDate = json2map(new HashMap(), jsonStr, String.class, Long.class);
        Date dd = new Date(mapDate.get("time"));
        return dd;
    }

    /**
     * 将date转json字符串
     *
     * @param dd 待转换的Date实例对象
     * @return 转换后的JSon字符串
     */
    private static String date2json(Date dd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dd);
        //        Log.i("ZF",calendar.toString());
        Map<String, Long> map = new LinkedHashMap<String, Long>();
        map.put("date", Long.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        map.put("day", Long.valueOf(calendar.get(Calendar.DAY_OF_WEEK) - 1));
        map.put("hours", Long.valueOf(calendar.get(Calendar.HOUR)));
        map.put("minutes", Long.valueOf(calendar.get(Calendar.MINUTE)));
        map.put("month", Long.valueOf(calendar.get(Calendar.MONTH)));
        map.put("seconds", Long.valueOf(calendar.get(Calendar.SECOND)));
        map.put("time", dd.getTime());
        map.put("timezoneOffset", (long) 0 - (calendar.get(Calendar.ZONE_OFFSET) * 60 / (3600 *
                1000)));
        map.put("year", Long.valueOf(calendar.get(Calendar.YEAR) - 1900));
        return map2json(map);
    }

    /**
     * Set 转json字符串
     *
     * @param set 待转换的Set实例对象
     * @return 转换后的JSon字符串
     */
    private static String set2json(Set set) {
        if (set.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            int count = 1;
            for (Iterator it = set.iterator(); it.hasNext(); ) {
                Object obj = it.next();
                sb.append(obj2json(obj));
                if (count != set.size()) sb.append(",");
                count++;
            }
            sb.append("]");
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * List 转json字符串
     *
     * @param list 待转换的List实例对象
     * @return 转换后的JSon字符串
     */
    private static String list2json(List list) {
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            int count = 1;
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                Object obj = it.next();
                sb.append(obj2json(obj));
                if (count != list.size()) sb.append(",");
                count++;
            }
            sb.append("]");
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Map 转json字符串
     *
     * @param map 待转换的map实例对象
     * @return 转换后的JSon字符串
     */
    private static String map2json(Map map) {
        if (map.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            int count = 1;
            for (Object obj : map.keySet()) {
                sb.append(obj2json(obj));
                sb.append(":");
                sb.append(obj2json(map.get(obj)));
                if (count != map.size()) sb.append(",");
                count++;
            }
            sb.append("}");
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * JSon字符串转Map的静态方法
     *
     * @param inputMap 输入Map
     * @param jsonStr  转换JSon字符串
     * @param keyClz   Map的键实例对象的字节码文件
     * @param valClz   Map的值实例对象的字节码文件
     * @return
     */
    private static Map json2map(Map inputMap, String jsonStr, Class keyClz, Class valClz) {
        //        Log.i("ZF",jsonStr);
        JSONObject jsonObject = null;
        Map rtnMap = inputMap;
        try {
            jsonObject = new JSONObject(jsonStr);
            for (Iterator it = jsonObject.keys(); it.hasNext(); ) {
                Object keyObj = it.next();
                Object valObj = jsonObject.getString(keyObj.toString());
                keyObj = json2obj(keyObj.toString(), keyClz);
                //                Log.i("ZF",keyObj.toString() + ":" + valObj.toString());
                valObj = json2obj(valObj.toString(), valClz);
                rtnMap.put(keyObj, valObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtnMap;
    }

    /**
     * JSon字符串转Set的静态方法
     *
     * @param inputSet 输入的Set
     * @param jsonStr  转换JSon字符串
     * @param itmClz   Set节点的Class字节码文件
     * @return
     */
    private static Set json2set(Set inputSet, String jsonStr, Class itmClz) {
        Set rtnSet = inputSet;
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                String itmStr = (String) jsonArray.get(i);
                Object itmObj = json2obj(itmStr, itmClz);
                rtnSet.add(itmObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtnSet;
    }

    /**
     * JSon字符串转List的静态方法
     *
     * @param inputList 输入的List
     * @param jsonStr   转换JSon字符串
     * @param itmClz    List节点的Class字节码文件
     * @return
     */
    private static List json2list(List inputList, String jsonStr, Class itmClz) {
        List rtnList = inputList;
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                String itmStr = jsonArray.get(i) + "";
                Object itmObj = json2obj(itmStr, itmClz);
                rtnList.add(itmObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtnList;
    }

    /**
     * 获取Field中的List,Set,Map中的泛型的类型
     *
     * @param field 包装对象中的成员变量Field
     * @param i     例如Map<String,Integer> i=0 返回java.lang.String字节码文件
     *              i=1 返回java.lang.Integer字节码文件
     * @return 取出的字节码文件
     * @throws RuntimeException
     * @throws ClassNotFoundException
     */
    public static Class getGenericType(Field field, Integer i)
            throws RuntimeException, ClassNotFoundException {
        Type mapMainType = field.getGenericType();

        if (mapMainType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) mapMainType;
            Type[] types = parameterizedType.getActualTypeArguments();
            if (i < types.length) {
                return Class.forName(types[i].toString().replace("class ", ""));
            } else {
                throw new RuntimeException("查询的类型数组超出边界!");
            }
        } else {
            throw new RuntimeException("获取泛型类型出错");
        }
    }

    /**
     * 移除首末位引号Quotation Mark
     *
     * @param inputStr
     * @return
     */
    private static String removeqm(String inputStr) {
        StringBuffer sb = new StringBuffer();
        sb.append(inputStr);
        if (sb.length() > 0) {                                          //去除首末的引号字符
            if (sb.charAt(sb.length() - 1) == '\"') {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (sb.charAt(0) == '\"') {
                sb.deleteCharAt(0);
            }
        }
        return sb.toString();
    }

    /**
     * 转换为指定Class字节码的数组
     * @param objs
     * @param clazz
     * @return
     */
    private static Object toAssignArray(Object objs,Class clazz) {
        int len = Array.getLength(objs);
        Object array = Array.newInstance(clazz,len);
        for(int i=0;i<len;i++) {
            Array.set(array,i,Array.get(objs,i));
        }
        return array;
    }

    /**
     * 装箱为指定的包装对象
     * @param objs
     * @param clazz
     * @return
     */
    private static Object[] toBoxArray(Object objs,Class clazz) {
        int len = Array.getLength(objs);
        Object[] outObjs = new Object[len];
        for(int i=0;i<len;i++) {
            Array.set(outObjs,i,(Object) Array.get(objs,i));
        }
        return outObjs;
    }
}
