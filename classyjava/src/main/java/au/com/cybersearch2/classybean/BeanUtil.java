/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classybean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.openbeans.BeanInfo;
import com.googlecode.openbeans.IntrospectionException;
import com.googlecode.openbeans.Introspector;
import com.googlecode.openbeans.PropertyDescriptor;

/**
 * BeanUtil
 * @author Andrew Bowley
 * 30/05/2014
 */
public class BeanUtil
{
    /**
     * DataPair
     * Entry for String, Object Map
     * @author Andrew Bowley
     * 28/07/2014
     */
    public static class DataPair extends AbstractMap.SimpleImmutableEntry<String, Object>
    {
        private static final long serialVersionUID = 6959237568599112142L;
        
        public DataPair(String key, Object value) 
        {
            super(key, value);
        }

    }

    /**
     * Static empty Object array to represent no parameters in reflection method call
     */
    public static final Object[] NO_ARGS = new Object[] {};

    /**
     * Returns the result of dynamically invoking this method. Equivalent to
     * {@code receiver.methodName(arg1, arg2, ... , argN)}.
     *
     * <p>If the method is static, the receiver argument is ignored (and may be null).
     *
     * <p>If the method takes no arguments, you can pass {@code (Object[]) null} instead of
     * allocating an empty array.
     *
     * <p>If you're calling a varargs method, you need to pass an {@code Object[]} for the
     * varargs parameter: that conversion is usually done in {@code javac}, not the VM, and
     * the reflection machinery does not do this for you. (It couldn't, because it would be
     * ambiguous.)
     *
     * <p>Reflective method invocation follows the usual process for method lookup.
     *
     * <p>If an exception is thrown during the invocation it is caught and
     * wrapped in an InvocationTargetException. This exception is then thrown.
     *
     * <p>If the invocation completes normally, the return value itself is
     * returned. If the method is declared to return a primitive type, the
     * return value is boxed. If the return type is void, null is returned.
     *
     * @param method
     *            the method to invoke
     * @param receiver
     *            the object on which to call this method (or null for static methods)
     * @param args
     *            the arguments to the method
     * @return the result
     *
     * @throws BeanException
     *             if this method is not accessible
     *             if the number of arguments doesn't match the number of parameters, the receiver
     *                is incompatible with the declaring class, or an argument could not be unboxed
     *                or converted by a widening conversion to the corresponding parameter type
     *             if an exception was thrown by the invoked method
     */
    public static Object invoke(Method method, Object receiver, Object... args)
            throws BeanException
    {
        try
        {
            return method.invoke(receiver, args);
        }
        catch (IllegalArgumentException e)
        {
            throw new BeanException("Invoke failed for method " + method.getName(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new BeanException("Invoke failed for method " + method.getName(), e);
        }
        catch (InvocationTargetException e)
        {
            throw new BeanException("Invoke failed for method " + method.getName(), e.getCause() == null ? e : e.getCause());
        }
    }

    /**
    * Gets the <code>BeanInfo</code> object which contains the information of
    * the properties, events and methods of the specified bean class.
    *
    * <p>
    * The <code>Introspector</code> will cache the <code>BeanInfo</code>
    * object. Subsequent calls to this method will be answered with the cached
    * data.
    * </p>
    *
    * @param bean The specified bean class.
    * @return the <code>BeanInfo</code> of the bean class.
    * @throws BeanException If introspection fails
    */ 
    public static BeanInfo getBeanInfo(Object bean)
    {
        BeanInfo info = null;
        try
        {
            info = Introspector.getBeanInfo(bean.getClass());
        }
        catch (IntrospectionException e)
        {
            throw new BeanException("Bean introspection failed for class " + bean.getClass().getName(), e);
        }
        return info;
    }
 
    /**
     * Returns bean properties as an Entry Set
     *@param bean The specified bean class.
     *@return Set&lt;DataPair&gt;
     */
    public static Set<DataPair> getDataPairSet(Object bean)
    {
        BeanInfo info = getBeanInfo(bean);
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        HashSet<DataPair> result = new HashSet<DataPair>(descriptors.length * 2);
        for (PropertyDescriptor property : descriptors) 
        {
            Method method = property.getReadMethod();
            if (method != null) // No getter defined if method == null
                result.add(new DataPair(property.getName(), invoke(method, bean, NO_ARGS)));
        }
        return result;
    }

    /**
     * Returns Object of specified class name
     *@param className Class name
     *@return Object
     *@throws BeanException if class not found, failed to instantiate or security violated
     */
    public static Object newClassInstance(String className)
    {
        if (className == null)
            throw new IllegalArgumentException("Parameter className is null");
        try
        {
            Class<?> newClass = Class.forName(className);
            Object newInstance = newClass.getConstructor().newInstance();
            return newInstance;
        }
        catch (ClassNotFoundException e)
        {
            throw new BeanException("Class " + className + " not found", e);
        }
        catch (InstantiationException e)
        {
            throw new BeanException("Failed to instantiate class " + className, e.getCause() == null ? e : e.getCause());
        }
        catch (IllegalAccessException | SecurityException e)
        {
            throw new BeanException("Security prevented creation of class " + className, e);
        } 
        catch (IllegalArgumentException e) 
        {
            throw new BeanException("A method has been passed an invalid argument", e);
		} 
        catch (InvocationTargetException e) 
        {
            throw new BeanException("Exception thrown while creating class " + className, e);
		} 
        catch (NoSuchMethodException e) 
        {
            throw new BeanException("Method call failed while creating class " + className, e);
		} 
    }
}
