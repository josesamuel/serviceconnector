package util.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a callback method to be called when a service gets connected or disconnected.
 * <p>
 * The method should have the signature <b>(String, boolean)</b>, a string for the serviceIntent
 * and a boolean as second parameter for service connected state
 * <p>
 * Methods with other signatures are ignored.
 * <p>
 * ex:
 * <pre><code>
 *  {@literal @}ServiceConnectionCallback
 *  public void onServiceConnectionChanged(String serviceIntent, boolean connected) {
 *  }
 * </code></pre>
 *
 * @author jsam
 * @see util.service.ServiceConnector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceConnectionCallback {}
