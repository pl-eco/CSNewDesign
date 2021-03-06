/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * An <code>AmbTypeNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type.
 */
public class AmbTypeNode_c extends TypeNode_c implements AmbTypeNode {
    protected QualifierNode qual;
    protected Id name;

    public AmbTypeNode_c(Position pos, QualifierNode qual, Id name) {
        super(pos);
        assert (name != null); // qual may be null
        this.qual = qual;
        this.name = name;
    }

    @Override
    public Id id() {
        return this.name;
    }

    @Override
    public AmbTypeNode id(Id name) {
        AmbTypeNode_c n = (AmbTypeNode_c) copy();
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return this.name.id();
    }

    @Override
    public AmbTypeNode name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public QualifierNode qual() {
        return this.qual;
    }

    @Override
    public AmbTypeNode qual(QualifierNode qual) {
        AmbTypeNode_c n = (AmbTypeNode_c) copy();
        n.qual = qual;
        return n;
    }

    protected AmbTypeNode_c reconstruct(QualifierNode qual, Id name) {
        if (qual != this.qual || name != this.name) {
            AmbTypeNode_c n = (AmbTypeNode_c) copy();
            n.qual = qual;
            n.name = name;
            return n;
        }

        return this;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        QualifierNode qual = (QualifierNode) visitChild(this.qual, v);
        Id name = (Id) visitChild(this.name, v);
        return reconstruct(qual, name);
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        if (qual != null && !qual.isDisambiguated()) {
            return this;
        }

        Node n =
                sc.nodeFactory()
                  .disamb()
                  .disambiguate(this, sc, position(), qual, name);

        if (n instanceof TypeNode) {
            return n;
        }

        throw new SemanticException("Could not find type \""
                + (qual == null ? name.toString() : qual.toString() + "."
                        + name.toString()) + "\".", position());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (qual != null) {
            print(qual, w, tr);
            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }

        tr.print(this, name, w);
    }

    @Override
    public String toString() {
        return (qual == null ? name.toString() : qual.toString() + "."
                + name.toString())
                + "{amb}";
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.AmbTypeNode(this.position, this.qual, this.name);
    }
}
