package com.nuonuo.iframe.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)/*保留的时间长短*/
@Target(ElementType.METHOD)
@Inherited/*只用于class，可被子类继承*/
public @interface Mapping {
    String value();
}
