package com.example.ioc.ioc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * ioc容器
 *
 * @author Quifar
 * @version V1.0
 **/
public class SimpleIoc {
    // ioc容器
    private Map<String, Object> iocContainer = new HashMap<>();

    public SimpleIoc(String xmlPath) throws Exception {
        loadBeans(xmlPath);
    }

    /**
     * 根据bean名称从容器中获取bean
     *
     * @param name bean名称
     * @return
     */
    public Object getBean(String name) {
        Object bean = iocContainer.get(name);
        if (bean == null) {
            throw new IllegalArgumentException("there is no bean with name " + name);
        }

        return bean;
    }

    /**
     * 根据类型获取bean
     * 注意：如果存在多个实例，将会抛出异常
     *
     * @param requiredType bean名称
     * @return
     */
    public <T> T getBean(Class<T> requiredType) {
        List<Object> beans = new ArrayList<>();
        iocContainer.forEach((k, v) -> {
            if (v.getClass() == requiredType) {
                beans.add(v);
            }
        });
        if (beans.size() > 1) {
            throw new IllegalArgumentException("the requiredType is not singleton");
        }
        return (T) beans.get(0);
    }

    /**
     * 加载xml中配置的beans
     *
     * @param xmlPath xml路径
     */
    private void loadBeans(String xmlPath) throws Exception {
        //解析xml文件
        InputStream inputStream = new FileInputStream(xmlPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        // 遍历 <bean> 标签
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String className = ele.getAttribute("class");

                // bean实例
                Object bean = newInstance(className);
                if (bean == null) {
                    throw new InstantiationException();
                }

                setProperties(bean, ele);
            }
        }
    }

    /**
     * 为bean的属性赋值
     *
     * @param bean 需要赋值的属性所属bean
     * @param ele  bean节点内容元素
     */
    private void setProperties(Object bean, Element ele) throws NoSuchFieldException, IllegalAccessException {
        NodeList propertyNodes = ele.getElementsByTagName("property");
        String id = ele.getAttribute("id");
        for (int i = 0, len = propertyNodes.getLength(); i < len; i++) {
            Node item = propertyNodes.item(i);
            if (item instanceof Element) {
                Element pe = (Element) item;
                String name = pe.getAttribute("name");
                String value = pe.getAttribute("value");

                // 利用反射将 bean 相关字段访问权限设为可访问(改变访问权限)
                // 为某个类中指定的域赋值
                Field declaredField = bean.getClass().getDeclaredField(name);
                declaredField.setAccessible(true);

                if (null != value && value.length() > 0) {
                    declaredField.set(bean, value);
                } else {
                    String ref = pe.getAttribute("ref");
                    if (ref == null || ref.length() == 0) {
                        throw new IllegalArgumentException("property must contain value or ref");
                    }
                    Object refBean = getBean(ref);
                    declaredField.set(bean, refBean);
                }
            }
        }
        registerBean(id, bean);
    }

    /**
     * 根据key 注册bean到IOC容器中
     *
     * @param beanName IOC容器中的bean的名称
     * @param bean     需要注册的bean
     */
    private void registerBean(String beanName, Object bean) {
        iocContainer.put(beanName, bean);
    }

    /**
     * 根据类名称利用反射机制实例化
     *
     * @param className 类名称(全限名)
     * @return
     */
    private Object newInstance(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException
                | IllegalAccessException
                | InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
