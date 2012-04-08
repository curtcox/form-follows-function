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

package org.f3.tools.tree;

import org.f3.api.tree.*;
import org.f3.api.tree.Tree.F3Kind;
import org.f3.api.tree.SyntheticTree.SynthType;

import com.sun.tools.mjavac.code.Type;
import com.sun.tools.mjavac.util.JCDiagnostic.DiagnosticPosition;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.sun.tools.mjavac.util.List;

//TODO for now we need this or classes like DiagnosticPosition are unhappy
import com.sun.tools.mjavac.tree.JCTree;
import com.sun.source.tree.TreeVisitor;



/**
 * The base of the F3 AST
 * well... except for things like statement which (at least for now) have to be subclassed
 * off other parts of the JCTree.
 */
public abstract class F3Tree extends JCTree implements SyntheticTree, Tree, Cloneable, DiagnosticPosition {

    /**
     * The Generated type of this node - for instance, was it synthesised by the compiler?
     */
    protected SynthType genType = null;

    /**
     * Tell the caller how/why this node was created.
     *
     * Nodes can be created because they reflect the source code input, or because they
     * are required to generate a complete Java class, or possibly, some other reason. By
     * default, we assume that a node was generated to reflect source code. Override this
     * method to return something different and set {@link genType} using setSynthType
     * at some point in the node creation sequence.
     *
     * @return The generation type of the node (derived from source code or some other reason).
     */
    public SynthType getGenType() {

        if  (genType == null) {
        
            setGenType(SynthType.COMPILED);
        }
        return genType;
    }

    /**
     * Allows the creator of a node to indicate the reason the node was created.
     *
     * Without explicitly setting the generation type using this method, a node
     * will inform any caller that is it was derived from the source code {@link SynthType.COMPILED}
     * rather than generated by the compiler itself for some internal reason.
     *
     * @param genType The new generation type that this node should advertise itself
     *                as from now on.
     */
    public void setGenType(SynthType genType) {
        this.genType = genType;
    }

    /* The tag of this node -- one of the constants declared above.
     */
    public int getTag() {
        throw new RuntimeException("bad call to getTag() - class: " + getClass() + ", F3 tag: " + getF3Tag());
    }

    /* The tag of this node -- one of the constants declared above.
     */
    public abstract F3Tag getF3Tag();

    /** Set position field and return this tree.
     */
    @Override
    public F3Tree setPos(int pos) {
        this.pos = pos;
        return this;
    }

    /** Set type field and return this tree.
     */
    @Override
    public F3Tree setType(Type type) {
        this.type = type;
        return this;
    }

    /** Get a default position for this tree node.
     */
    @Override
    public DiagnosticPosition pos() {
        return this;
    }

    // for default DiagnosticPosition
    @Override
    public F3Tree getTree() {
        return this;
    }

    @Override
    public int getStartPosition() {
        return F3TreeInfo.getStartPos(this);
    }

    @Override
    public int getEndPosition(Map<JCTree, Integer> endPosTable) {
        return F3TreeInfo.getEndPos(this, endPosTable);
    }

    // for default DiagnosticPosition
    @Override
    public int getPreferredPosition() {
        return pos;
    }

    /** Initialize tree with given tag.
     */
    protected F3Tree() {
    }
    
    public abstract void accept(F3Visitor v);
    
    /**
     * Gets the F3 kind of this tree.
     *
     * @return the kind of this tree.
     */
    public abstract F3Kind getF3Kind();
    
    @SuppressWarnings("unchecked")
    public static <T> java.util.List<T> convertList(Class<T> klass, com.sun.tools.mjavac.util.List<?> list) {
	if (list == null)
	    return null;
	for (Object o : list)
	    klass.cast(o);
        return (java.util.List<T>)list;
    }

    /** Convert a tree to a pretty-printed string. */
    @Override
    public String toString() {
        StringWriter s = new StringWriter();
        try {
            new F3Pretty(s, false).printExpr(this);
        }
        catch (IOException e) {
            // should never happen, because StringWriter is defined
            // never to throw any IOExceptions
            throw new AssertionError(e);
        }
        return s.toString();
    }
    
    /**
     * Was this tree expected, but missing, and filled-in by the parser
     */
    public boolean isMissing() {
        return false;
    }
    
    /**
     * Allow all nodes to become equivalent to Erronous by being able to
     * return any Erroneous error nodes they are holding (default they don't have any).
     */
    public List<? extends F3Tree> getErrorTrees() {
        return List.<F3Tree>nil();
    }

    /****
     * Make JCTree happy
     */
    
    public Kind getKind() {
        throw new UnsupportedOperationException("Use getF3Kind() not getKind() - class: " + getClass() + ", F3 tag: " + getF3Tag());
    }

    public void accept(Visitor v) {
        throw new UnsupportedOperationException("We don't use visitors from JCTree - class: " + getClass() + ", F3 tag: " + getF3Tag());
    }

    public <R,D> R accept(TreeVisitor<R,D> v, D d) {
        throw new UnsupportedOperationException("We don't use visitors from JCTree - class: " + getClass() + ", F3 tag: " + getF3Tag());
    }

}
