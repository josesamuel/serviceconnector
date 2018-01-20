package util.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a callback method to be called when bind to service fails
 * <p>
 * The method should have the signature <b>(String, Exception)</b>, a string for the serviceIntent
 * and an Exception as second parameter for bind failure exception
 * <p>
 * Methods with other signatures are ignored.
 * <p>
 * ex:
 * <pre><code>
 *  {@literal @}ServiceConnectionFailureCallback
 *  public void onServiceConnectionFailed(String serviceIntent, Exception exception) {
 *  }
 * </code></pre>
 *
 * @author jsam
 * @see util.service.ServiceConnector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceConnectionFailureCallback {}
