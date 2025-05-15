package archit.common;

import archit.parser.ArchitParser;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Type {
    private final String name;
    private final Class<?> equivalent;

    private Type(String name, Class<?> equivalent) {
        this.name = name;
        this.equivalent = equivalent;
    }

    public final String getBaseName() {
        return name;
    }

    public final Class<?> getEquivalent() {
        return equivalent;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Type) {
            return toString().equals(o.toString());
        }
        return false;
    }

    public static class ListType extends Type {  // NOSONAR
        private final Type elements;

        private ListType(Type elements) {
            super("list", List.class);
            this.elements = elements;
        }

        public final Type getElements() {
            return elements;
        }

        @Override
        public String toString() {
            return "[" + elements + "]";
        }

        @Override
        public ListType asListType() {
            return this;
        }
    }

    public static class MapType extends Type {  // NOSONAR
        private final Type key;
        private final Type value;

        private MapType(Type key, Type value) {
            super("map", Map.class);
            this.key = key;
            this.value = value;
        }

        public final Type getKey() {
            return key;
        }

        public final Type getValue() {
            return value;
        }

        @Override
        public String toString() {
            var sb = new StringBuilder();
            sb.append('|');
            sb.append(key);
            sb.append(" -> ");
            sb.append(value);
            sb.append('|');
            return sb.toString();
        }

        @Override
        public MapType asMapType() {
            return this;
        }
    }

    public static class LiteralType extends Type {
        private final SortedSet<String> members;

        private LiteralType(String[] members) {
            super("literal", String.class);
            this.members = Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(members)));
        }

        public final SortedSet<String> getMembers() {
            return members;
        }

        public final boolean contains(String member) {
            return members.contains(member);
        }

        @Override
        public String toString() {
            return "<" + String.join(", ", members) + ">";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof LiteralType;
        }

        @Override
        public LiteralType asLiteralType() {
            return this;
        }
    }

    public static final Type number = new Type("number", BigInteger.class);
    public static final Type real = new Type("real", Double.class);
    public static final Type logic = new Type("logic", Boolean.class);
    public static final Type string = new Type("string", String.class);
    public static final Type material = new Type("material", Material.class);

    public static Type list(Type elements) {
        return new ListType(elements);
    }

    public ListType asListType() {
        return null;
    }

    public static Type map(Type key, Type value) {
        return new MapType(key, value);
    }

    public MapType asMapType() {
        return null;
    }

    // chciałem nazwać 'enum' jak w prezentacji, ale jest to słowo kluczowe...
    public static Type literal(String... values) {
        return new LiteralType(values);
    }

    public LiteralType asLiteralType() {
        return null;
    }

    public static Type fromTypeContext(ArchitParser.TypeContext ctx) {
        if (ctx.listType() != null) {
            var subtype = fromTypeContext(ctx.listType().type());
            return list(subtype);
        } else if (ctx.mapType() != null) {
            var key = fromTypeContext(ctx.mapType().type(0));
            var value = fromTypeContext(ctx.mapType().type(1));
            return map(key, value);
        } else if (ctx.enumType() != null) {
            var members = ctx.enumType().ID().stream().map(TerminalNode::getText).toArray(String[] ::new);
            return literal(members);
        }

        return switch (ctx.primitive.getText()) {
            case "number" -> number;
            case "real" -> real;
            case "logic" -> logic;
            case "string" -> string;
            case "material" -> material;
            default -> throw new IllegalArgumentException("Unknown type (did the grammar change?)");
        };
    }
}
