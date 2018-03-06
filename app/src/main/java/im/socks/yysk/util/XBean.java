package im.socks.yysk.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * HashMap是无序的，LinkedHashMap保持插入的顺序，也就是在叠加的时候，按插入的顺序输出
 *
 * @author cole
 */
@SuppressWarnings("unchecked")
public class XBean extends HashMap<Object, Object> {

    private static final long serialVersionUID = -1820845323415950533L;

    /**
     * 构造一个新的对象
     */
    public XBean() {
    }

    /**
     * 复制一个Map，对这个XBean操作，不会影响原来的Map
     *
     * @param map
     */
    public XBean(Map<?, ?> map) {
        putAll(map);
    }

    /**
     * 简便的构造函数，可以设置多个属性，如：new XBean("a",1,"b",2)<br>
     * new XBean((Object)list)) list的长度必须是2的倍数，不能够使用XBean(list)，因为有特殊的定义
     *
     * @param args 类型为ListArgs,如：new XBean("a",1,"b",2)，必须是2的倍数
     */
    public XBean(Object... args) {
        set(args);
    }

    /**
     * 简便的方法，把XBean对象转化为任何Map的类型，否则有时候范型非常的烦
     *
     * @return
     */
    public <K, V> Map<K, V> cast() {
        return (Map<K, V>) this;
    }

    /**
     * 复制map中的多个属性到当前对象
     *
     * @param map  需要复制的map
     * @param keys ListArgs类型
     * @return 返回this，方便级联操作
     */
    public XBean setAll(Map<?, ?> map, Object... keys) {
        for (Object key : ListArgs.wrap(keys)) {
            put(key, map.get(key));
        }
        return this;
    }

    /**
     * 设置多个值，如：set("a",1,"b",2)
     *
     * @param args ListArgs，长度必须是2的倍数
     * @return 返回this对象
     */
    public XBean set(Object... args) {
        return X.set(this, args);
    }

    /**
     * @param names ListArgs, 一次取消多个key
     * @return 返回this对象
     */
    public XBean unset(Object... keys) {
        return X.unset(this, keys);
    }

    /**
     * @param keys ListArgs，从当前的map中获得指定的几个key的值
     * @return 返回一个新的对象
     */
    public XBean fetch(Object... keys) {
        return X.fetch(this, keys);
    }

    /**
     * 仅仅返回key对应的值的第一个，在url参数的时候使用，如：map={"a":[1],"b":[1]} fetchOne("a","b") =>
     * {"a":1,"b":1}
     *
     * @param keys
     * @return
     */
    public XBean fetchOne(Object... keys) {
        return X.fetchOne(this, keys);
    }

    public XBean removeNullValues() {
        Iterator<Entry<Object, Object>> entries = entrySet().iterator();
        while (entries.hasNext()) {
            Entry<Object, Object> entry = entries.next();
            if (entry.getValue() == null) {
                entries.remove();
            }
        }
        return this;
    }

    public XBean getXBean(Object key) {
        return X.getOne(this, key, XBean.class);
    }

    /**
     * 获得key对应的值且转换为指定的类型，如果不需要转换类型，使用get(key)，
     * 只要类型不是List，Array，get(key,type)==getOne(key,type)
     *
     * @param key
     * @param type
     * @return
     */
    public <T> T get(Object key, Class<T> type) {
        return X.get(this, key, type);
    }

    public <T> T get(Object key, Class<T> type, T defaultValue) {
        return X.get(this, key, type, defaultValue);
    }

    public <T> T getOne(Object key) {
        return X.getOne(this, key);
    }

    public <T> T getOne(Object name, Class<T> type) {
        return X.getOne(this, name, type);
    }

    public <T> T getOne(Object key, Class<T> type, T defaultValue) {
        return X.getOne(this, key, type, defaultValue);
    }

    // ================= time ==================
    public TimeZone getTimeZone(Object key) {
        return X.getOne(this, key, TimeZone.class);
    }

    public TimeZone getTimeZone(Object name, TimeZone defaultValue) {
        return X.getOne(this, name, TimeZone.class, defaultValue);
    }

    /**
     * 得到一个Date对象，支持long和字符串(yyyy-mm-dd hh:mm:ss)转换
     *
     * @param name
     * @return
     */
    public Date getDate(Object name) {
        return X.getOne(this, name, Date.class);
    }

    /**
     * 得到一个Date对象，支持long和字符串(yyyy-mm-dd hh:mm:ss)转换
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Date getDate(Object name, Date defaultValue) {
        return X.getOne(this, name, Date.class, defaultValue);
    }


    /**
     * 得到一个Date对象，支持long和字符串(yyyy-mm-dd)转换
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public java.sql.Date getSQLDate(Object name) {
        return X.getOne(this, name, java.sql.Date.class);
    }

    /**
     * 得到一个Date对象，支持long和字符串(yyyy-mm-dd)转换
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public java.sql.Date getSQLDate(Object name, java.sql.Date defaultValue) {
        return X.getOne(this, name, java.sql.Date.class, defaultValue);
    }

    /**
     * 得到一个Time对象，支持long和字符串(hh:mm:ss)转换
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Time getTime(Object name) {
        return X.getOne(this, name, Time.class);
    }

    /**
     * 得到一个Time对象，支持long和字符串(hh:mm:ss)转换
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Time getTime(Object name, Time defaultValue) {
        return X.getOne(this, name, Time.class, defaultValue);
    }

    /**
     * 得到一个Timestamp对象，支持long和字符串(yyyy-mm-dd hh:mm:ss)转换
     *
     * @param name
     * @return
     */
    public Timestamp getTimestamp(Object name) {
        return X.getOne(this, name, Timestamp.class);
    }

    /**
     * 得到一个Timestamp对象，支持long和字符串(yyyy-mm-dd hh:mm:ss)转换
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Timestamp getTimestamp(Object name, Timestamp defaultValue) {
        return X.getOne(this, name, Timestamp.class, defaultValue);
    }

    // =================== number ===================

    public BigDecimal getBigDecimal(Object key) {
        return X.getOne(this, key, BigDecimal.class);
    }

    public BigDecimal getBigDecimal(Object name, BigDecimal defaultValue) {
        return X.getOne(this, name, BigDecimal.class, defaultValue);
    }

    public BigInteger getBigInteger(Object name) {
        return X.getOne(this, name, BigInteger.class);
    }

    public BigInteger getBigInteger(Object name, BigInteger defaultValue) {
        return X.getOne(this, name, BigInteger.class, defaultValue);
    }

    public Byte getByte(Object name) {
        return X.getOne(this, name, Byte.class);
    }

    public Byte getByte(Object name, Byte defaultValue) {
        return X.getOne(this, name, Byte.class, defaultValue);
    }

    public Short getShort(Object name) {
        return X.getOne(this, name, Short.class);
    }

    public Short getShort(Object name, Short defaultValue) {
        return X.getOne(this, name, Short.class, defaultValue);
    }

    public Character getCharacter(Object name) {
        return X.getOne(this, name, Character.class);
    }

    public Character getCharacter(Object name, Character defaultValue) {
        return X.getOne(this, name, Character.class, defaultValue);
    }

    public Integer getInteger(Object name) {
        return X.getOne(this, name, Integer.class);
    }

    public Integer getInteger(Object name, Integer defaultValue) {
        return X.getOne(this, name, Integer.class, defaultValue);

    }

    public Long getLong(Object name) {
        return X.getOne(this, name, Long.class);
    }

    public Long getLong(Object name, Long defaultValue) {
        return X.getOne(this, name, Long.class, defaultValue);
    }

    public Float getFloat(Object name) {
        return X.getOne(this, name, Float.class);
    }

    public Float getFloat(Object name, Float defaultValue) {
        return X.getOne(this, name, Float.class, defaultValue);
    }

    public Double getDouble(Object name) {
        return X.getOne(this, name, Double.class);
    }

    public Double getDouble(Object name, Double defaultValue) {
        return X.getOne(this, name, Double.class, defaultValue);
    }

    public Boolean getBoolean(Object name) {
        return X.getOne(this, name, Boolean.class);
    }

    public Boolean getBoolean(Object key, Boolean defaultValue) {
        return X.getOne(this, key, Boolean.class, defaultValue);
    }

    public String getString(Object key) {
        return X.getOne(this, key, String.class);
    }

    public String getString(Object key, String defaultValue) {
        return X.getOne(this, key, String.class, defaultValue);
    }

    /**
     * 获得一个enum的对象，支持字符串的转换
     *
     * @param <T>
     * @param name
     * @param type
     * @return
     */
    public <T> T getEnum(Object name, Class<T> type) {
        return X.getOne(this, name, type);
    }

    /**
     * 获得一个enum的对象，支持字符串的转换
     *
     * @param <T>
     * @param key
     * @param type
     * @param defaultValue
     * @return
     */
    public <T> T getEnum(Object key, Class<T> type, T defaultValue) {
        return X.getOne(this, key, type, defaultValue);
    }

    /**
     * 判断key对应的值是否为null或者空字符串
     *
     * @param key
     * @return true表示为null或者空字符串
     */
    public boolean isNull(Object key) {
        return X.isNull(this, key);
    }

    /**
     * 判断key对应的值是否为null或者空List，数组
     *
     * @param key
     * @return true表示为null或者空List或者数组
     */
    public boolean isEmpty(Object key) {
        return X.isEmpty(this, key);
    }

    /**
     * 替换存在的key，如果key不存在，不替换
     *
     * @param oldKey
     * @param newKey
     * @return 返回true表示替换成功，false表示没有替换
     */
    public boolean replaceKey(Object oldKey, Object newKey) {
        return X.replaceKey(this, oldKey, newKey);
    }

    /**
     * 属性的值返回为一个集合，元素的类型不做任何的转换
     *
     * @param <T>
     * @param name 属性的名字
     * @return 返回一个新的List对象
     */
    public <T> List<T> getList(Object name) {
        return X.getList(this, name);

    }

    /**
     * 返回一个新的List对象，元素转换为指定的类型
     *
     * @param <T>
     * @param name
     * @param elementType
     * @return 返回一个新的List对象，如果属性不存在，返回null
     */
    public <T> List<T> getList(Object name, Class<T> elementType) {
        return X.getList(this, name, elementType);
    }

    public <T> List<T> getList(Object name, Class<T> elementType, T elementDefaultValue) {
        return X.getList(this, name, elementType, elementDefaultValue);
    }

    /**
     * 判断name对应的元素的长度，如：<br>
     * 如果name对应的元素不存在，返回0，如果对应的元素是集合，返回集合的长度，如果 对应的元素是数组，返回数组的长度，如果对应的元素只有一个，返回1
     *
     * @param name
     * @return
     */
    public int getSize(Object key) {
        return X.getSize(this, key);
    }

    /**
     * 转换所有的属性为指定的类型
     *
     * @param <T>
     * @param type
     * @param defaultValue
     * @param keys         ListArgs，如果没有，表示全部的keys
     */
    public <T> void toAll(Class<T> type, T defaultValue, Object... keys) {
        X.toAll(this, type, defaultValue, keys);
    }

    /**
     * 判断属性的值是否为指定的值，自动转换为value的类型后比较，使用getOne(name)获得值
     *
     * @param name
     * @param value
     * @return true表示相等，false表示不等
     */
    public boolean isEquals(Object name, Object value) {
        return X.isEquals(this, name, value);
    }

    /**
     * 简便的方法，如果属性为指定的值，那么，使用新的值替换
     *
     * @param name
     * @param value
     * @param newValue
     * @return
     */
    public boolean isEquals(Object name, Object value, Object newValue) {
        return X.isEquals(this, name, value, newValue);
    }

    /**
     * @param names 类型为ListArgs
     * @return 如果所有的属性都设置了，返回true，否则，返回false
     */
    public boolean hasKeys(Object... keys) {
        return X.hasKeys(this, keys);
    }

    /**
     * @param keys
     * @return 只要有一个key对应的值为null或者空字符串，返回true，否则返回false
     */
    public boolean hasNull(Object... keys) {
        return X.hasNull(this, keys);
    }

    /**
     * 删除所有的null值或者空字符串或者空集合（数组，Collection）
     *
     * @param trim true表示字符串需要trim，false表示不需要
     */
    public void removeNull() {
        X.removeNull(this);
    }

    /**
     * 把属性设置到obj对象，如：name =>
     * obj.setName("")，考虑到方法的名字的处理可能不同，会先查找setName，如果有下"_",如：x_y => setXY(),
     * url=>setUrl(),setURL() 对于不符合要求的属性，需要手动设置了
     *
     * @param obj
     */
    public void fill(Object obj) {
        X.fill(this, obj);
    }

    /**
     * 把obj的字段的值设置到map中
     *
     * @param obj
     * @param names 类型为ListArgs，元素类型为String，如果为null或者空数组，表示全部的字段
     * @return 返回obj
     */
    public <T> T pull(T obj, Object... names) {
        return X.pull(this, obj, names);
    }

    /**
     * 把map的值设置到对象中
     *
     * @param obj   设置到该对象
     * @param names 类型为ListArgs，元素类型为String，如果为null或者空数组，表示全部的字段
     * @return 返回obj
     */
    public <T> T push(T obj, Object... names) {
        return X.push(this, obj, names);
    }

}
