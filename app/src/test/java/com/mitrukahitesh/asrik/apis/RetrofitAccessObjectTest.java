/*
    Test if RetrofitAccessObject is following singleton pattern
 */

package com.mitrukahitesh.asrik.apis;

import static org.junit.Assert.*;

import org.junit.Test;

public class RetrofitAccessObjectTest {
    @Test
    public void singletonTest() {
        assertSame(RetrofitAccessObject.getRetrofitAccessObject(), RetrofitAccessObject.getRetrofitAccessObject());
    }
}