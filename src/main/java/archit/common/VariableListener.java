package archit.common;

import archit.parser.ArchitBaseListener;
import archit.parser.ArchitParser;

public class VariableListener extends ArchitBaseListener {
    private final VariableTable variableTable;

    public VariableListener(VariableTable variableTable) {
        this.variableTable = variableTable;
    }

    @Override
    public void enterVarDecl(ArchitParser.VarDeclContext ctx){
        variableTable.declareVariable(ctx.ID().getText(), ctx.type().getText());
    }


}
