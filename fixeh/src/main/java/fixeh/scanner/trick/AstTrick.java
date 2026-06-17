package fixeh.scanner.trick;

import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public interface AstTrick extends RecursiveTrick<CtElement> {
    /**
     * Apply this trick to AST (here we use spoon AST CtElement)
     * @param element AST to apply this trick
     * @return new AST after this trick's applied
     * @throws Exception when any exception occurs and we can't recover
     */
    @Override
    CtElement apply(CtElement element) throws Exception;
}
