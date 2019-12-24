import value.*;

import java.util.List;
import java.util.Optional;

public class IntImp extends ImpBaseVisitor<Value> {

    private final Conf conf;

    public IntImp(Conf conf) {
        this.conf = conf;
    }

    private ComValue visitCom(ImpParser.ComContext ctx) {
        return (ComValue) visit(ctx);
    }

    private ExpValue<?> visitExp(ImpParser.ExpContext ctx) {
        return (ExpValue<?>) visit(ctx);
    }

    private int visitNatExp(ImpParser.ExpContext ctx) {
        try {
            return ((NaturalValue) visit(ctx)).toJavaValue();
        }
        catch(ClassCastException e) {
            System.err.println("Type mismatch in the expression:");
            System.err.println();
            System.err.println(ctx.getText());
            System.err.println();
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getStartIndex());
            System.err.println("> Numerical expression expected");
            System.exit(1);
        }
        return 0; // dumb return (non-reachable code)
    }

    private boolean visitBoolExp(ImpParser.ExpContext ctx) {
        try {
            return ((BooleanValue) visit(ctx)).toJavaValue();
        }
        catch(ClassCastException e) {
            System.err.println("Type mismatch:");
            System.err.println(">>>>");
            System.err.println(ctx.getText());
            System.err.println("<<<<");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
            System.err.println("> Boolean expression expected");

            System.exit(1);
        }

        return false; // dumb return (non-reachable code)
    }

    @Override
    public ComValue visitProg(ImpParser.ProgContext ctx) {
        return visitCom(ctx.com());
    }

    @Override
    public ComValue visitIf(ImpParser.IfContext ctx) {
        if(visitBoolExp(ctx.exp())) {
            return visitCom(ctx.com(0));
        } else {
            if(ctx.getTokens(ImpParser.ELSE).stream().findFirst().isPresent()) {
                return visitCom(ctx.com(1));
            }
        }
        return ComValue.INSTANCE;
    }

    //TODO Mi sa che puo essere mergiato con quello sopra
    @Override
    public Value visitElseif(ImpParser.ElseifContext ctx) {
        List<ImpParser.ExpContext> expressions = ctx.exp();
        for(int i= 0; i < expressions.size(); i++) {
            if(visitBoolExp(expressions.get(i))) {
                return visitCom(ctx.com(i));
            }
        }
        if (!ctx.getTokens(ImpParser.ELSE).isEmpty()) {
            return visitCom(ctx.com(expressions.size()));
        }
        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitAssign(ImpParser.AssignContext ctx) {
        conf.put(ctx.ID().getText(), visitExp(ctx.exp()));
        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitSkip(ImpParser.SkipContext ctx) {
        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitSeq(ImpParser.SeqContext ctx) {
        visitCom(ctx.com(0));
        visitCom(ctx.com(1));
        return ComValue.INSTANCE;
    }

    private ComValue visitLoop (ImpParser.ExpContext expContext, ImpParser.ComContext com, Optional<ImpParser.ComContext> lastCommand) {
        if (!visitBoolExp(expContext))
            return ComValue.INSTANCE;
        visitCom(com);
        lastCommand.ifPresent(c -> visitCom(c));
        visitLoop(expContext, com, lastCommand);
        return null;
    }

    @Override
    public ComValue visitWhile(ImpParser.WhileContext ctx) {
        return visitLoop(ctx.exp(), ctx.com(), Optional.empty());
    }

    @Override
    public Value visitFor(ImpParser.ForContext ctx) {
        visitAssign((ImpParser.AssignContext) ctx.com(0));
        return visitLoop(ctx.exp(), ctx.com(2), Optional.of(ctx.com(1)));
    }

    @Override
    public ComValue visitDowhile(ImpParser.DowhileContext ctx) {
        visitCom(ctx.com());
        if (!visitBoolExp(ctx.exp()))
            return ComValue.INSTANCE;
        return visitCom(ctx);
    }

    @Override
    public Value visitNotdeterm(ImpParser.NotdetermContext ctx) {
        int one = (int) Math.round(Math.random());
        if(one == 0) {
            visitCom(ctx.com(0));
        } else {
            visitCom(ctx.com(1));
        }
        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitOut(ImpParser.OutContext ctx) {
        System.out.println(visitExp(ctx.exp()));
        return ComValue.INSTANCE;
    }

    @Override
    public NaturalValue visitNat(ImpParser.NatContext ctx) {
        return new NaturalValue(Integer.parseInt(ctx.NAT().getText()));
    }

    @Override
    public BooleanValue visitBool(ImpParser.BoolContext ctx) {
        return new BooleanValue(Boolean.parseBoolean(ctx.BOOL().getText()));
    }

    @Override
    public ExpValue<?> visitParExp(ImpParser.ParExpContext ctx) {
        return visitExp(ctx.exp());
    }

    @Override
    public ExpValue<?> visitNot(ImpParser.NotContext ctx) {
        return new BooleanValue(!visitBoolExp(ctx.exp()));
    }

    @Override
    public NaturalValue visitPow(ImpParser.PowContext ctx) {
        int base = visitNatExp(ctx.exp(0));
        int exp = visitNatExp(ctx.exp(1));

        return new NaturalValue((int) Math.pow(base, exp));
    }

    @Override
    public NaturalValue visitDivMulMod(ImpParser.DivMulModContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        switch (ctx.op.getType()) {
            case ImpParser.DIV : return new NaturalValue(left / right);
            case ImpParser.MUL : return new NaturalValue(left * right);
            case ImpParser.MOD : return new NaturalValue(left % right);
        }

        return null; // dumb return (non-reachable code)
    }

    @Override
    public NaturalValue visitPlusMinus(ImpParser.PlusMinusContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        switch (ctx.op.getType()) {
            case ImpParser.PLUS  : return new NaturalValue(left + right);
            case ImpParser.MINUS : return new NaturalValue(Math.max(left - right, 0));
        }

        return null; // dumb return (non-reachable code)
    }

    @Override
    public BooleanValue visitCmpExp(ImpParser.CmpExpContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        switch (ctx.op.getType()) {
            case ImpParser.GEQ : return new BooleanValue(left >= right);
            case ImpParser.LEQ : return new BooleanValue(left <= right);
            case ImpParser.LT  : return new BooleanValue(left < right);
            case ImpParser.GT  : return new BooleanValue(left > right);
        }

        return null; // dumb return (non-reachable code)
    }

    @Override
    public BooleanValue visitEqExp(ImpParser.EqExpContext ctx) {
        ExpValue<?> left = visitExp(ctx.exp(0));
        ExpValue<?> right = visitExp(ctx.exp(1));

        switch (ctx.op.getType()) {
            case ImpParser.EQQ : return new BooleanValue(left.equals(right));
            case ImpParser.NEQ : return new BooleanValue(!left.equals(right));
        }

        return null; // dumb return (non-reachable code)
    }

    @Override
    public BooleanValue visitLogicExp(ImpParser.LogicExpContext ctx) {
        boolean left = visitBoolExp(ctx.exp(0));
        boolean right = visitBoolExp(ctx.exp(1));

        switch (ctx.op.getType()) {
            case ImpParser.AND : return new BooleanValue(left && right);
            case ImpParser.OR  : return new BooleanValue(left || right);
        }

        return null; // dumb return (non-reachable code)
    }

    @Override
    public ExpValue<?> visitId(ImpParser.IdContext ctx) {
        if (conf.get(ctx.ID().getText()) == null) {
            System.err.println("Variable '" + ctx.ID().getText() + "' used but never declared");
            System.exit(1);
        }

        return conf.get(ctx.ID().getText());
    }
}
