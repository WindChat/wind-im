package com.windchat.im.business.bean;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * customized annotation
 *
 * @author Librena
 * @since 2020-10-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiActions {

    String action();

}
