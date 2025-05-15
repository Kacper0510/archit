package archit.common;

import archit.parser.ArchitParser.FunctionDeclContext;
import archit.parser.ArchitParser.FunctionParamContext;
import archit.parser.ArchitParser.VarDeclContext;
import org.antlr.v4.runtime.ParserRuleContext;

public interface Scope {
    ArchitFunction resolveFunction(String name, Type[] params);
    Variable resolveVariable(String name);
    boolean defineVariable(String name, Type type, int id, VarDeclContext ctx);
    boolean defineVariable(String name, Type type, int id, FunctionParamContext ctx);
    boolean defineFunction(String name, Type returnType, Type[] params, String[] paramNames, FunctionDeclContext ctx);
    Scope getParent();

    public record Variable(Type type, int id, ParserRuleContext declaration) {}
}
