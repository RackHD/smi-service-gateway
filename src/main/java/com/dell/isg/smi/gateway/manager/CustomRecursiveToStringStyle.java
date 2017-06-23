/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.gateway.manager;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author prashanth.gowda
 *
 */

@SuppressWarnings("serial")
public class CustomRecursiveToStringStyle extends ToStringStyle {

    private static final CustomRecursiveToStringStyle INSTANCE = new CustomRecursiveToStringStyle(13);


    public static ToStringStyle getInstance() {
        return INSTANCE;
    }


    public static String toString(Object value) {
        final StringBuffer sb = new StringBuffer(512);
        INSTANCE.appendDetail(sb, null, value);
        return sb.toString();
    }

    private final int maxDepth;
    private final String tabs;

    private ThreadLocal<MutableInteger> depth = new ThreadLocal<MutableInteger>() {
        @Override
        protected MutableInteger initialValue() {
            return new MutableInteger(0);
        }
    };


    public CustomRecursiveToStringStyle(int maxDepth) {
        this.maxDepth = maxDepth;
        tabs = StringUtils.repeat("\t", maxDepth);

        setUseShortClassName(true);
        setUseIdentityHashCode(false);
        setContentStart(" {");
        setFieldSeparator(SystemUtils.LINE_SEPARATOR);
        setFieldSeparatorAtStart(true);
        setFieldNameValueSeparator(" = ");
        setContentEnd("}");
    }


    private int getDepth() {
        return depth.get().get();
    }


    private void padDepth(StringBuffer buffer) {
        buffer.append(tabs, 0, getDepth());
    }


    private StringBuffer appendTabified(StringBuffer buffer, String value) {
        Matcher matcher = Pattern.compile("\n").matcher(value);
        String replacement = "\n" + tabs.substring(0, getDepth());
        while (matcher.find()) {
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer;
    }


    @Override
    protected void appendFieldSeparator(StringBuffer buffer) {
        buffer.append(getFieldSeparator());
        padDepth(buffer);
    }


    @Override
    public void appendStart(StringBuffer buffer, Object object) {
        depth.get().increment();
        super.appendStart(buffer, object);
    }


    @Override
    public void appendEnd(StringBuffer buffer, Object object) {
        super.appendEnd(buffer, object);
        buffer.setLength(buffer.length() - getContentEnd().length());
        buffer.append(SystemUtils.LINE_SEPARATOR);
        depth.get().decrement();
        padDepth(buffer);
        appendContentEnd(buffer);
    }


    @Override
    protected void removeLastFieldSeparator(StringBuffer buffer) {
        int len = buffer.length();
        int sepLen = getFieldSeparator().length() + getDepth();
        if (len > 0 && sepLen > 0 && len >= sepLen) {
            buffer.setLength(len - sepLen);
        }
    }


    private boolean noReflectionNeeded(Object value) {
        try {
            return value != null && (value.getClass().getName().startsWith("java.lang.") || value.getClass().getMethod("toString").getDeclaringClass() != Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (getDepth() >= maxDepth || noReflectionNeeded(value)) {
            appendTabified(buffer, String.valueOf(value));
        } else {
            new ReflectionToStringBuilder(value, this, buffer, null, false, false).toString();
        }
    }


    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection<?> coll) {
        buffer.append(ReflectionToStringBuilder.toString(coll.toArray(), this, true, true));
    }

    static class MutableInteger {
        private int value;


        MutableInteger(int value) {
            this.value = value;
        }


        public final int get() {
            return value;
        }


        public final void increment() {
            ++value;
        }


        public final void decrement() {
            --value;
        }
    }
}