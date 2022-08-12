package com.example.demo.helpers;

import com.example.demo.entities.Employee;
import com.example.demo.repositories.EmployeeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Component
public class StoredDBHelper {
    private static EmployeeRepository repository;
    private static ClassLoader classLoader = StoredDBHelper.class.getClassLoader();

    public StoredDBHelper(EmployeeRepository repository) {
        this.repository = repository;
    }

    public static <T> void exportAll() throws IOException {


        FileWriter fw;
        File file = new File(classLoader.getResource(".").getFile() + "/test.xml");
        if (file.createNewFile()) {
            fw = new FileWriter(file, false);
            fw.write("");
        } else {
            fw = new FileWriter(file, true);
        }


        for (Employee e : repository.findAll()) {
            fw.write(getDataEntity(e));
        }

    }

    private static String getDataEntity(Employee e) {
        return "";
    }

    @SuppressWarnings("rawtypes")
    public static Class<?> getEntity(JpaRepository repo) {
        Type clazzes = getGenericType(repo.getClass())[0];
        Type[] jpaClass = getGenericType(getClass(clazzes));
        return getClass(((ParameterizedType) jpaClass[0]).getActualTypeArguments()[0]);
    }

    public static Type[] getGenericType(Class<?> target) {
        if (target == null)
            return new Type[0];
        Type[] types = target.getGenericInterfaces();
        if (types.length > 0) {
            return types;
        }
        Type type = target.getGenericSuperclass();
        if (type != null) {
            if (type instanceof ParameterizedType) {
                return new Type[]{type};
            }
        }
        return new Type[0];
    }

    /*
     * Get the underlying class for a type, or null if the type is a variable
     * type.
     *
     * @param type
     * @return the underlying class
     */
    @SuppressWarnings("rawtypes")
    private static Class<?> getClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
