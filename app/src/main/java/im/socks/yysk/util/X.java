package im.socks.yysk.util;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.socks.yysk.util.Json.IJsonable;

public class X /* XMap */ {
    private static Map<Class<?>, Class<?>> PRIMITIVE_TYPES = new HashMap<>();
    private static Map<Class<?>, Object> DEFAULT_VALUES = new HashMap<>();

    static {
        PRIMITIVE_TYPES.put(boolean.class, Boolean.class);
        PRIMITIVE_TYPES.put(byte.class, Byte.class);
        PRIMITIVE_TYPES.put(char.class, Character.class);
        PRIMITIVE_TYPES.put(short.class, Short.class);
        PRIMITIVE_TYPES.put(int.class, Integer.class);
        PRIMITIVE_TYPES.put(long.class, Long.class);
        PRIMITIVE_TYPES.put(float.class, Float.class);
        PRIMITIVE_TYPES.put(double.class, Double.class);
        PRIMITIVE_TYPES.put(void.class, Void.class);

        DEFAULT_VALUES.put(boolean.class, Boolean.FALSE);
        DEFAULT_VALUES.put(byte.class, (byte) 0);
        DEFAULT_VALUES.put(char.class, (char) 0);
        DEFAULT_VALUES.put(short.class, (short) 0);
        DEFAULT_VALUES.put(int.class, (int) 0);
        DEFAULT_VALUES.put(long.class, (long) 0);
        DEFAULT_VALUES.put(float.class, (float) 0);
        DEFAULT_VALUES.put(double.class, (double) 0);

        PRIMITIVE_TYPES = Collections.unmodifiableMap(PRIMITIVE_TYPES);
        DEFAULT_VALUES = Collections.unmodifiableMap(DEFAULT_VALUES);
    }

    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * @param type 如果是primiteve类型，转换为对象类型
     * @return
     */
    public static Class getType(Class type) {
        if (type.isPrimitive()) {
            return (Class) PRIMITIVE_TYPES.get(type);
        } else {
            return type;
        }
    }

    /**
     * @param pairs ["a",1,"b",2] => {"a":1,"b":2}
     * @return
     */
    public static Map map(Object... pairs) {
        List list = ListArgs.wrap(pairs);
        Map map = new HashMap<>();
        for (int i = 0; i < list.size(); ) {
            map.put(list.get(i), list.get(i + 1));
            i += 2;
        }
        return map;
    }

    /**
     * @param pairs ["a",1,"b",2] => [{"a":1},{"b":2}]
     * @return
     */
    public static List<Map> pair(Object... pairs) {
        List list = new ArrayList<>();
        for (int i = 0; i < pairs.length; ) {
            list.add(map(pairs[i], pairs[i + 1]));
            i += 2;
        }
        return list;
    }

    /**
     * 创建一个List，如：list(1,2,3) => [1,2,3]
     *
     * @param objs
     * @return 返回一个新的List对象
     */
    public static <T> List<T> list(Object... objs) {
        return toList(objs);
    }

    /**
     * 转换为List，如：toList(1) => [1] , toList([1,2,3]) => [1,2,3]
     *
     * @param obj，可以为单值，或者集合，List，数组等
     * @return 返回一个新的List对象，如果obj为null，返回一个空白的对象
     */
    public static <T> List<T> toList(Object value) {
        List<T> list = new ArrayList<>();
        if (value == null) {
            //
        } else if (value instanceof Iterable<?>) {
            Iterable<T> iter = (Iterable<T>) value;
            for (T item : iter) {
                list.add(item);
            }
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                list.add((T) Array.get(value, i));
            }

        } else {
            list.add((T) value);
        }
        return list;

    }

    public static <T> List<T> toList(Object value, Class<T> elementType) {
        return toList(value, elementType, getDefaultValue(elementType));
    }

    public static <T> List<T> toList(Object value, Class<T> elementType, T elementDefaultValue) {
        List list = toList(value);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, to(list.get(i), elementType, elementDefaultValue));
        }
        return list;
    }

    /**
     * 获得key对应的值，且转换为指定的类型
     *
     * @param map
     * @param key
     * @param type
     * @return 如果没有值或者不能够转换为指定的类型，返回null，如果是primitive，返回0或者false
     */
    public static <T> T get(Map<?, ?> map, Object key, Class<T> type) {
        return to(map.get(key), type, getDefaultValue(type));
    }

    /**
     * 获得值且转换为指定的类型
     *
     * @param map
     * @param key
     * @param type
     * @param defaultValue
     * @return
     */
    public static <T> T get(Map<?, ?> map, Object key, Class<T> type, T defaultValue) {
        return to(map.get(key), type, defaultValue);
    }

    /**
     * 获得第一个值
     *
     * @param map
     * @param key
     * @return
     */
    public static <T> T getOne(Map<?, ?> map, Object key) {
        Object value = map.get(key);
        Object firstValue = null;
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            firstValue = list.size() > 0 ? list.get(0) : null;
        } else if (value != null && value.getClass().isArray()) {
            firstValue = Array.getLength(value) > 0 ? Array.get(value, 0) : null;
        } else {
            firstValue = value;
        }
        return (T) firstValue;
    }

    /**
     * 获得第一个值，转换为指定的类型，默认值为null，如果是primitive，默认值为0或者false
     *
     * @param map
     * @param key
     * @param type
     * @return
     */
    public static <T> T getOne(Map<?, ?> map, Object key, Class<T> type) {
        return to(getOne(map, key), type, getDefaultValue(type));
    }

    /**
     * 获得第一个值且转换为指定类型
     *
     * @param map
     * @param key
     * @param type
     * @param defaultValue
     * @return
     */
    public static <T> T getOne(Map<?, ?> map, Object key, Class<T> type, T defaultValue) {

        return to(getOne(map, key), type, defaultValue);
    }

    /**
     * 获得key对应的值，使用List封装
     *
     * @param map
     * @param key
     * @return 如果key没有设置或者为null，返回null，否则返回一个新的List
     */
    public static <T> List<T> getList(Map<?, ?> map, Object key) {
        Object value = map.get(key);
        return (List<T>) (value != null ? toList(value) : null);
    }

    /**
     * 获得key对应的list，转换元素类型
     *
     * @param map
     * @param key
     * @param elementType
     * @return
     */
    public static <T> List<T> getList(Map<?, ?> map, Object key, Class<T> elementType) {
        return toList(map.get(key), elementType, getDefaultValue(elementType));
    }

    public static <T> List<T> getList(Map<?, ?> map, Object key, Class<T> elementType, T elementDefaultValue) {
        return toList(map.get(key), elementType, elementDefaultValue);
    }

    /**
     * @param value
     * @param type  如果是primitive，默认类型为0，false，如果是对象类型，默认为null
     * @return
     */
    public static <T> T to(Object value, Class<T> type) {
        return to(value, type, getDefaultValue(type));
    }

    public static <T> T to(Object value, Class<T> type, T defaultValue) {
        checkDefaultValue(type, defaultValue);
        if (isNumberType(type)) {
            return toNumber(value, type, defaultValue);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) toBoolean(value, (Boolean) defaultValue);
        } else if (type == String.class) {
            return (T) toString(value, (String) defaultValue);
        } else if (isTimeType(type)) {
            return toTime(value, type, defaultValue);
        } else if (isEnum(type)) {
            return toEnum(value, type, defaultValue);
        } else {
            if (type.isInstance(value)) {
                return type.cast(value);
            } else {
                return defaultValue;
            }
        }

        // return defaultValue;

    }

    /**
     * 转换多个key为指定的类型
     *
     * @param map
     * @param type
     * @param defaultValue
     * @param keys         ListArgs，如果没有，表示全部的key
     */
    public static <T> void toAll(Map map, Class<T> type, T defaultValue, Object... keys) {
        List list = ListArgs.wrap(keys);
        if (list.isEmpty()) {
            list = new ArrayList(map.keySet());
        }
        for (Object name : list) {
            Object value = getOne(map, name, type, defaultValue);
            map.put(name, value);
        }
    }

    /**
     * 检查指定类型
     *
     * @param type
     * @param defaultValue
     */
    private static void checkDefaultValue(Class<?> type, Object defaultValue) {
        if (type.isPrimitive() && defaultValue == null) {
            throw new IllegalArgumentException("primitive类型的默认值不能够为null");
        }
        if (defaultValue == null) {
            return;
        }
        type = getObjectType(type);
        if (!type.isInstance(defaultValue)) {
            throw new IllegalArgumentException(
                    "指定的类型和默认值的类型不兼容：" + type.getName() + "," + defaultValue.getClass().getName());
        }
    }

    private static <T> T getDefaultValue(Class<T> type) {
        if (type.isPrimitive()) {
            return (T) DEFAULT_VALUES.get(type);
        } else {
            return null;
        }
    }

    /**
     * 如果类型是primitive的，返回其对象的类型，其它的，直接返回
     *
     * @param type
     * @return
     */
    public static <T> Class<T> getObjectType(Class<?> type) {
        return type.isPrimitive() ? (Class<T>) PRIMITIVE_TYPES.get(type) : (Class<T>) type;
    }

    private static boolean isNumberType(Class<?> type) {
        type = getObjectType(type);
        return type == Byte.class || type == Short.class || type == Integer.class || type == Long.class
                || type == Float.class || type == Double.class || type == BigInteger.class || type == BigDecimal.class
                || type == Character.class;
    }

    private static boolean isIntegerType(Class<?> type) {
        type = getObjectType(type);
        return type == Byte.class || type == Short.class || type == Integer.class || type == Long.class
                || type == BigInteger.class || type == BigDecimal.class;
    }

    /**
     * 字符串可以转换为指定的数字类型<br>
     * BigDecimal可以转换为BigInteger<br>
     * byte,char,short,int,long 可以转换为float，double<br>
     * float,double,BigDecimal不可以转换为byte,char,short,int,long<br>
     * float,double,BigDecimal可以互相转换<br>
     *
     * @param <T>
     * @param name
     * @param type
     * @param defaultValue
     * @return
     */
    public static <T> T toNumber(Object value, Class<T> type, T defaultValue) {
        checkDefaultValue(type, defaultValue);
        type = getObjectType(type);
        if (!isNumberType(type)) {
            throw new IllegalArgumentException("type不是Number的类型：" + type.getName());
        }
        if (value == null) {
            return defaultValue;
        }
        // String s = null;
        if (type.isInstance(value)) {
            if (type == Float.class) {
                float v = (Float) value;
                if (Float.isInfinite(v) || Float.isNaN(v)) {
                    return defaultValue;
                }
            } else if (type == Double.class) {
                double v = (Double) value;
                if (Double.isInfinite(v) || Double.isNaN(v)) {
                    return defaultValue;
                }
            }
            return (T) value;
        }

        if (value instanceof Character) {
            value = ((Integer) (int) ((Character) value).charValue()).toString();
        }

        if (value instanceof Number) {
            Number n = (Number) value;
            if (type == Byte.class) {
                return (T) (Byte) n.byteValue();
            } else if (type == Short.class) {
                return (T) (Short) n.shortValue();
            } else if (type == Integer.class) {
                return (T) (Integer) n.intValue();
            } else if (type == Long.class) {
                return (T) (Long) n.longValue();
            } else if (type == Float.class) {
                return (T) (Float) n.floatValue();
            } else if (type == Double.class) {
                return (T) (Double) n.doubleValue();
            } else if (type == BigInteger.class) {
                if (value instanceof BigDecimal) {
                    return (T) ((BigDecimal) value).toBigInteger();
                } else {
                    return (T) BigInteger.valueOf(n.longValue());
                }
            } else if (type == BigDecimal.class) {
                if (value instanceof BigInteger) {
                    return (T) new BigDecimal(((BigInteger) value));
                } else {
                    return (T) BigDecimal.valueOf(n.doubleValue());
                }
            } else {
                return defaultValue;
            }

        } else if (value instanceof String) {
            // 也可以不使用这个
            String s = (String) value;
            s = s.trim();
            if (s.length() == 0) {
                return defaultValue;
            }
            try {
                if (type == Byte.class) {
                    return (T) (Byte) Byte.parseByte(s);
                } else if (type == Character.class) {
                    int v = Integer.parseInt(s);
                    // 如果值超出了范围，就返回默认值
                    if (v < Character.MIN_VALUE || v > Character.MAX_VALUE) {
                        return defaultValue;
                    }
                    return (T) (Character) (char) v;
                } else if (type == Short.class) {
                    return (T) (Short) Short.parseShort(s);
                } else if (type == Integer.class) {
                    return (T) (Integer) Integer.parseInt(s);
                } else if (type == Long.class) {
                    return (T) (Long) Long.parseLong(s);
                } else if (type == Float.class) {
                    float v = Float.parseFloat(s);
                    if (Float.isInfinite(v) || Float.isNaN(v)) {
                        return defaultValue;
                    }
                    return (T) (Float) v;
                } else if (type == Double.class) {
                    double v = Double.parseDouble(s);
                    if (Double.isInfinite(v) || Double.isNaN(v)) {
                        return defaultValue;
                    }
                    return (T) (Double) v;
                } else if (type == BigDecimal.class) {
                    return (T) new BigDecimal(s);
                } else if (type == BigInteger.class) {
                    return (T) new BigInteger(s);
                } else {
                    throw new RuntimeException("不可能执行到这里");
                }
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }

    }

    /**
     * 转换为boolean
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static Boolean toBoolean(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String s = (String) value;
            s = s.trim();
            if (s.length() == 0) {
                return defaultValue;
            }
            if ("true".equals(s) || "1".equals(s)) {
                return Boolean.TRUE;
            } else if ("false".equals(s) || "0".equals(s)) {
                return Boolean.FALSE;
            } else {
                return defaultValue;
            }
        }

        return defaultValue;

    }

    /**
     * 转换为字符串对象,如果为null或者空字符串，返回默认值
     *
     * @param value
     * @param defaultValue
     * @return 如果value为null或者空字符串，返回默认值，如果是Date，返回long，如果是Map,List,[]，使用json编码，
     * 如果是其他的，使用value.toString();
     */
    public static String toString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            String s = (String) value;
            return s.isEmpty() ? defaultValue : s;
        }
        // 添加了下面这些处理，更加好的支持类型
        else if (value instanceof Date) {
            return Long.toString(((Date) value).getTime());
        } else if (value instanceof IJsonable || value instanceof Map || value instanceof Iterable<?>
                || value.getClass().isArray()) {
            return Json.stringify(value);
        } //
        else {
            // 常用的类型，如：number,boolean等
            return value.toString();
        }

    }

    /**
     * 把属性设置到obj对象，如：name =>
     * obj.setName("")，考虑到方法的名字的处理可能不同，会先查找setName，如果有下"_",如：x_y => setXY(),
     * url=>setUrl(),setURL() 对于不符合要求的属性，需要手动设置了
     *
     * @param map
     * @param obj
     */
    public static void fill(Map<?, ?> map, Object obj) {
        Class type = obj.getClass();
        Method[] methods = type.getMethods();
        for (Object key : map.keySet()) {
            String name = (String) key;
            // a_b_c => ABC
            String[] parts = name.split("_");
            StringBuilder buf = new StringBuilder();
            for (String part : parts) {
                buf.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
            }
            name = buf.toString();
            // setAbcXyz
            Method method = findMethod(type, methods, "set" + name);
            if (method == null) {
                // setURL
                method = findMethod(type, methods, "set" + name.toUpperCase());
            }
            if (method != null) {
                Class valueType = method.getParameterTypes()[0];
                Object value = get(map, key, valueType, getDefaultValue(valueType));
                try {
                    method.invoke(obj, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private static Method findMethod(Class<?> type, Method[] methods, String name) {
        Method targetMethod = null;
        for (Method method : methods) {
            int m = method.getModifiers();
            if (!Modifier.isPublic(m)) {
                continue;
            }
            if (Modifier.isStatic(m)) {
                continue;
            }
            if (Modifier.isAbstract(m)) {
                continue;
            }

            if (method.getParameterTypes() == null || method.getParameterTypes().length != 1) {

                continue;
            }
            if (method.getName().equals(name)) {
                // 这里先返回最近的类的方法，实际上也不需要判断的，因为methods的排序就是最近的类的方法在前面，所以遇到的第一个方法就是最近的类的
                if (method.getDeclaringClass() == type) {
                    return method;
                }
                // base class中提供的，先不返回
                if (targetMethod == null) {
                    targetMethod = method;
                } else if (targetMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                    // 子类的方法优先
                    targetMethod = method;
                } else {
                    //
                }

            }

        }
        return targetMethod;
    }

    /**
     * 判断name对应的元素的长度，如：<br>
     * 如果name对应的元素不存在，返回0，如果对应的元素是集合，返回集合的长度，如果 对应的元素是数组，返回数组的长度，如果对应的元素只有一个，返回1
     *
     * @param name
     * @return
     */
    public static int getSize(Map map, Object name) {
        Object value = map.get(name);
        if (value == null) {
            return 0;
        }

        if (value instanceof Collection) {
            return ((Collection) value).size();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value);
        } else {
            return 1;
        }
    }

    /**
     * @param map
     * @param keys ListArgs，从当前的map中获得指定的几个key的值
     * @return 返回一个新的对象，类型和Map的一致
     */
    public static <T extends Map> T fetch(T map, Object... keys) {
        List<Object> list = ListArgs.wrap(keys);
        Iterable<Object> iter = null;
        if (list.isEmpty()) {
            iter = map.keySet();
        } else {
            iter = list;
        }
        try {
            Map newMap = map.getClass().newInstance();
            for (Object key : iter) {
                newMap.put(key, map.get(key));
            }
            return (T) newMap;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("", e);
        }

    }

    /**
     * 如果key对应的值是List，数组，仅仅返回第一个，主要是值url参数的时候使用
     *
     * @param map
     * @param keys ListArgs
     * @return
     */
    public static <T extends Map> T fetchOne(T map, Object... keys) {
        List<Object> list = ListArgs.wrap(keys);
        Iterable<Object> iter = null;
        if (list.isEmpty()) {
            iter = map.keySet();
        } else {
            iter = list;
        }
        try {
            T newMap = (T) map.getClass().newInstance();
            for (Object key : iter) {
                newMap.put(key, getOne(map, key));
            }
            return newMap;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("", e);
        }

    }

    /**
     * 判断key(s)对应的元素是不是null或者空字符串（如果对应的类型为集合，是否存在一个，且第一个不能够为null或者空字符串）
     *
     * @param keys ListArgs
     * @return 如果有一个key对应的值为null或者空字符串，返回ture，否则返回flase
     */
    public static boolean isNull(Map<?, ?> map, Object key) {
        Object value = getOne(map, key);
        if (value == null) {
            return true;
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            return true;
        }
        return false;

    }

    public static boolean isEmpty(Map<?, ?> map, Object key) {
        return getSize(map, key) == 0;
    }

    /**
     * @param args ListArgs类型
     * @return 返回this对象
     */
    public static <T extends Map> T set(T map, Object... args) {
        List<?> list = ListArgs.wrap(args);
        if (list.size() % 2 != 0) {
            throw new IllegalArgumentException("元素的个数必须是2的倍数");
        }

        for (int i = 0; i < list.size(); ) {
            map.put(list.get(i), list.get(i + 1));
            i += 2;
        }
        return (T) map;

    }

    public static <T extends Map> T unset(T map, Object... keys) {
        for (Object key : ListArgs.wrap(keys)) {
            map.remove(key);
        }
        return (T) map;
    }

    public static boolean hasKeys(Map map, Object... keys) {
        List<?> list = ListArgs.wrap(keys);
        for (Object key : list) {
            if (!map.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasValues(Map<?, ?> map, Object... values) {
        List<?> list = ListArgs.wrap(values);
        for (Object value : list) {
            if (!map.containsValue(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param map
     * @param keys
     * @return 只要有一个key对应的值为null或者空字符串，返回true，否则返回false
     */
    public static boolean hasNull(Map<?, ?> map, Object... keys) {
        for (Object key : ListArgs.wrap(keys)) {
            if (isNull(map, key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除map中为null或者空字符串的key
     *
     * @param map
     */
    public static void removeNull(Map<?, ?> map) {
        for (Object key : new ArrayList<>(map.keySet())) {
            if (isNull(map, key)) {
                map.remove(key);
            }
        }
    }

    /**
     * 把obj的字段的值设置到map中
     *
     * @param obj
     * @param names 类型为ListArgs，元素类型为String，如果为null或者空数组，表示全部的字段
     * @return 返回obj
     * @parma map
     */
    public static <T> T pull(Map map, T obj, Object... names) {

        List<String> nameList = ListArgs.wrap(names);
        // 所有的字段，包括继承的
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            int m = field.getModifiers();
            if (!Modifier.isPublic(m)) {
                continue;
            }
            if (Modifier.isStatic(m)) {
                continue;
            }

            String name = field.getName();
            if (nameList.size() > 0 && !nameList.contains(name)) {
                continue;
            }

            try {
                Object value = field.get(obj);
                map.put(name, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("", e);
            }

        }
        return obj;

    }

    /**
     * 把map的值设置到对象中
     *
     * @param obj   设置到该对象
     * @param names 类型为ListArgs，元素类型为String，如果为null或者空数组，表示全部的字段
     * @return 返回obj
     */
    public static <T> T push(Map map, T obj, Object... names) {
        List<String> nameList = ListArgs.wrap(names);
        // 所有的字段，包括继承的
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            int m = field.getModifiers();
            if (!Modifier.isPublic(m)) {
                continue;
            }
            if (Modifier.isStatic(m)) {
                continue;
            }

            String name = field.getName();
            if (nameList.size() > 0 && !nameList.contains(name)) {
                continue;
            }
            if (!map.containsKey(name)) {
                continue;
            }
            Object value = get(map, name, (Class) field.getType(), (Object) getDefaultValue(field.getType()));
            Class type = field.getType();
            if (value == null && (type == List.class || type == Map.class)) {
                // 如果不能够直接转化为类型，可能是json类型
                value = Json.parse((String) map.get(name), type);
            }

            try {
                field.set(obj, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("", e);
            }

        }
        return obj;

    }

    /**
     * 转换对象为enum类型，支持字符串的转换
     *
     * @param <T>
     * @param value
     * @param type
     * @param defaultValue
     * @return
     */
    public static <T> T toEnum(Object value, Class type, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        if (value instanceof String) {
            String s = (String) value;
            s = s.trim();
            if (s.isEmpty()) {
                return defaultValue;
            }
            try {
                return (T) Enum.valueOf(type, (String) value);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private static boolean isEnum(Class type) {
        return type.isEnum();
    }

    private static boolean isTimeType(Class type) {
        return type == Date.class || type == java.sql.Date.class || type == Time.class || type == Timestamp.class;

    }

    public static <T> T toTime(Object value, Class<T> type, T defaultValue) {
        checkDefaultValue(type, defaultValue);
        if (!isTimeType(type)) {
            throw new IllegalArgumentException("type必须是java.util.Date的子类，现在为：" + type.getName());
        }
        if (value == null) {
            return defaultValue;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        long time = 0;
        String s = null;
        if (isIntegerType(value.getClass())) {
            time = ((Number) value).longValue();
        } else if (value instanceof String) {
            s = (String) value;
            try {
                time = Long.parseLong(s);
                s = null;
            } catch (NumberFormatException e) {
                time = 0;
            }
        } else if (Date.class.isInstance(value)) {
            // 支持Date之间的互转
            time = ((Date) value).getTime();
        } else {
            return defaultValue;
        }

        if (time > 0) {
            if (type == Date.class) {
                return (T) new Date(time);
            } else if (type == java.sql.Date.class) {
                return (T) new java.sql.Date(time);
            } else if (type == Time.class) {
                return (T) new Time(time);
            } else if (type == Timestamp.class) {
                return (T) new Timestamp(time);
            } else {
                throw new IllegalArgumentException("不支持的类型:" + type.getName());
            }
        } else if (s != null) {
            if (type == Date.class) {
                return (T) new Date(Timestamp.valueOf(s).getTime());
            } else if (type == java.sql.Date.class) {
                return (T) java.sql.Date.valueOf(s);
            } else if (type == Time.class) {
                return (T) Time.valueOf(s);
            } else if (type == Timestamp.class) {
                return (T) Timestamp.valueOf(s);
            } else {
                throw new IllegalArgumentException("不支持的类型:" + type.getName());
            }
        } else {
            return defaultValue;
        }

    }

    /**
     * 替换存在的key，如果key不存在，不替换
     *
     * @param oldKey
     * @param newKey
     * @return 返回true表示替换成功，false表示没有替换
     */
    public static boolean replaceKey(Map map, Object oldKey, Object newKey) {
        if (map.containsKey(oldKey)) {
            Object value = map.remove(oldKey);
            map.put(newKey, value);
            return true;
        }
        return false;

    }

    /**
     * 判断属性的值是否为指定的值，自动转换为value的类型后比较，使用getOne(name)获得值
     *
     * @param name
     * @param value 不能够为null
     * @return
     */
    public static boolean isEquals(Map map, Object name, Object value) {
        if (value == null) {
            return getOne(map, name) == null;
        }
        Object oldValue = getOne(map, name, value.getClass());
        if (oldValue == value) {
            return true;
        }
        return value.equals(oldValue);
    }

    /**
     * 简便的方法，如果属性为指定的值，那么，使用新的值替换
     *
     * @param name
     * @param value
     * @param newValue
     * @return
     */
    public static boolean isEquals(Map map, Object name, Object value, Object newValue) {
        if (isEquals(map, name, value)) {
            map.put(name, newValue);
            return true;
        }
        return false;
    }

}
