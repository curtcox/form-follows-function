/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package f3.reflect;

/** Represent a JVM primitive type.
 *
 * @author Per Bothner
 * @profile desktop
 */
public class F3PrimitiveType extends F3Type {
    Class clas;
    String name;
    Class boxed;
    F3PrimitiveType(Class clas, String name, Class boxed) {
        this.clas = clas;
        this.name = name;
	this.boxed = boxed;
    }
    public String getName() { return name; }

    static final F3PrimitiveType voidType =
        new F3PrimitiveType(Void.TYPE, "Void", Void.class);

    static final F3PrimitiveType byteType =
	new F3PrimitiveType(Byte.TYPE, "Byte", Byte.class);
    
    static final F3PrimitiveType shortType =
        new F3PrimitiveType(Short.TYPE, "Short", Short.class);
    
    static final F3PrimitiveType integerType =
        new F3PrimitiveType(Integer.TYPE, "Integer", Integer.class);

    static final F3PrimitiveType longType =
        new F3PrimitiveType(Byte.TYPE, "Long", Long.class);

    static final F3PrimitiveType floatType =
        new F3PrimitiveType(Float.TYPE, "Float", Float.class);
    
    static final F3PrimitiveType doubleType =
        new F3PrimitiveType(Double.TYPE, "Double", Double.class);

    static final F3PrimitiveType charType =
        new F3PrimitiveType(Character.TYPE, "Character", Character.class);

    static final F3PrimitiveType booleanType =
        new F3PrimitiveType(Boolean.TYPE, "Boolean", Boolean.class);

    static final F3PrimitiveType numberType = floatType;

    public Class getBoxed() {
	return boxed;
    }

    public F3PrimitiveValue mirrorOf(Object value) {
        if (this == integerType || this == shortType || this == byteType ||
                this == charType)
            return new F3IntegerValue(((Number) value).intValue(), this);
        if (this == longType)
            return new F3LongValue(((Number) value).longValue(), this);
        if (this == floatType)
            return new F3FloatValue(((Number) value).floatValue(), this);
        if (this == doubleType)
            return new F3DoubleValue(((Number) value).doubleValue(), this);
        if (this == booleanType)
            return new F3BooleanValue(((Boolean) value).booleanValue(), this);
        return null; // Should never happen.
    }
};
