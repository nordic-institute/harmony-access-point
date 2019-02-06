package eu.domibus.core.security;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Locale.ENGLISH;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class DomibusProvider extends Provider {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusProvider.class);
    public static final String CERT_PATH_VALIDATOR = "CertPathValidator";
    public static final String PKIX = "PKIX";

    private BouncyCastleProvider bouncyCastleProvider;

    @Autowired
    private DssService dssService;

    protected DomibusProvider() {
        super("DOMBIBUS", 1, "test");
        bouncyCastleProvider = new BouncyCastleProvider();
    }


    public synchronized void putService(Service s) {
        super.putService(s);
    }


    /**
     * Returns the name of this provider.
     *
     * @return the name of this provider.
     */
    public String getName() {
        return bouncyCastleProvider.getName();
    }

    /**
     * Returns the version number for this provider.
     *
     * @return the version number for this provider.
     */
    public double getVersion() {
        return bouncyCastleProvider.getVersion();
    }

    /**
     * Returns a human-readable description of the provider and its
     * services.  This may return an HTML page, with relevant links.
     *
     * @return a description of the provider and its services.
     */
    public String getInfo() {
        return bouncyCastleProvider.getInfo();
    }


    @Override
    public void clear() {
        bouncyCastleProvider.clear();
    }


    @Override
    public void load(InputStream inStream) throws IOException {
        bouncyCastleProvider.load(inStream);
    }


    @Override
    public void putAll(Map<?, ?> t) {
        bouncyCastleProvider.putAll(t);
    }

    /**
     * Returns an unmodifiable Set view of the property entries contained
     * in this Provider.
     *
     * @see java.util.Map.Entry
     * @since 1.2
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return bouncyCastleProvider.entrySet();
    }

    @Override
    public Set<Object> keySet() {
        return bouncyCastleProvider.keySet();
    }

    @Override
    public Collection<Object> values() {
        return bouncyCastleProvider.values();
    }


    @Override
    public  Object put(Object key, Object value) {
        return bouncyCastleProvider.put(key, value);
    }

    @Override
    public synchronized Service getService(String type, String algorithm) {
        LOG.info("type:[{}], algorithm:[{}]",type,algorithm);
        if(PKIX.equals(algorithm) && CERT_PATH_VALIDATOR.equals(type)){
            return dssService;
        }
        return bouncyCastleProvider.getService(type,algorithm);
    }

    @Override
    public synchronized Set<Service> getServices() {
        return bouncyCastleProvider.getServices();
    }


    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the string {@code "putProviderProperty."+name},
     * where {@code name} is the provider name, to see if it's ok to set this
     * provider's property values.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values.
     * @since 1.8
     */
    @Override
    public  Object putIfAbsent(Object key, Object value) {
        return bouncyCastleProvider.putIfAbsent(key, value);
    }

    /**
     * Removes the {@code key} property (and its corresponding
     * {@code value}).
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the string {@code "removeProviderProperty."+name},
     * where {@code name} is the provider name, to see if it's ok to remove this
     * provider's properties.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to remove this provider's properties.
     * @since 1.2
     */
    @Override
    public  Object remove(Object key) {
        return bouncyCastleProvider.remove(key);
    }

    @Override
    public  boolean remove(Object key, Object value) {
        return bouncyCastleProvider.remove(key, value);
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the string {@code "putProviderProperty."+name},
     * where {@code name} is the provider name, to see if it's ok to set this
     * provider's property values.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values.
     * @since 1.8
     */
    @Override
    public  boolean replace(Object key, Object oldValue,
                                        Object newValue) {
        return bouncyCastleProvider.replace(key, oldValue, newValue);
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the string {@code "putProviderProperty."+name},
     * where {@code name} is the provider name, to see if it's ok to set this
     * provider's property values.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values.
     * @since 1.8
     */
    @Override
    public  Object replace(Object key, Object value) {
        return bouncyCastleProvider.replace(key, value);
    }

    /**
     * Replaces each entry's value with the result of invoking the given
     * function on that entry, in the order entries are returned by an entry
     * set iterator, until all entries have been processed or the function
     * throws an exception.
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the string {@code "putProviderProperty."+name},
     * where {@code name} is the provider name, to see if it's ok to set this
     * provider's property values.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values.
     * @since 1.8
     */
    @Override
    public  void replaceAll(BiFunction<? super Object, ? super Object, ? extends Object> function) {
        bouncyCastleProvider.replaceAll(function);

    }

    /**
     * Attempts to compute a mapping for the specified key and its
     * current mapped value (or {@code null} if there is no current
     * mapping).
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the strings {@code "putProviderProperty."+name}
     * and {@code "removeProviderProperty."+name}, where {@code name} is the
     * provider name, to see if it's ok to set this provider's property values
     * and remove this provider's properties.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values or remove properties.
     * @since 1.8
     */
    @Override
    public  Object compute(Object key,
                                       BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return bouncyCastleProvider.compute(key, remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value (or
     * is mapped to {@code null}), attempts to compute its value using
     * the given mapping function and enters it into this map unless
     * {@code null}.
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the strings {@code "putProviderProperty."+name}
     * and {@code "removeProviderProperty."+name}, where {@code name} is the
     * provider name, to see if it's ok to set this provider's property values
     * and remove this provider's properties.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values and remove properties.
     * @since 1.8
     */
    @Override
    public  Object computeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
        return bouncyCastleProvider.computeIfAbsent(key, mappingFunction);
    }

    /**
     * If the value for the specified key is present and non-null, attempts to
     * compute a new mapping given the key and its current mapped value.
     *
     * <p>If a security manager is enabled, its {@code checkSecurityAccess}
     * method is called with the strings {@code "putProviderProperty."+name}
     * and {@code "removeProviderProperty."+name}, where {@code name} is the
     * provider name, to see if it's ok to set this provider's property values
     * and remove this provider's properties.
     *
     * @throws SecurityException if a security manager exists and its {@link
     *                           java.lang.SecurityManager#checkSecurityAccess} method
     *                           denies access to set property values or remove properties.
     * @since 1.8
     */
    @Override
    public  Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return bouncyCastleProvider.computeIfPresent(key, remappingFunction);
    }


    @Override
    public Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return bouncyCastleProvider.merge(key, value, remappingFunction);
    }

    // let javadoc show doc from superclass
    @Override
    public Object get(Object key) {
        return bouncyCastleProvider.get(key);
    }

    /**
     * @since 1.8
     */
    @Override
    public  Object getOrDefault(Object key, Object defaultValue) {
        return bouncyCastleProvider.getOrDefault(key, defaultValue);
    }

    /**
     * @since 1.8
     */
    @Override
    public  void forEach(BiConsumer<? super Object, ? super Object> action) {
        bouncyCastleProvider.forEach(action);
    }

    // let javadoc show doc from superclass
    @Override
    public Enumeration<Object> keys() {
        return bouncyCastleProvider.keys();
    }

    // let javadoc show doc from superclass
    @Override
    public Enumeration<Object> elements() {
        return bouncyCastleProvider.elements();
    }

    // let javadoc show doc from superclass
    public String getProperty(String key) {
        return bouncyCastleProvider.getProperty(key);
    }



}
