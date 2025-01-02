package Boxed;

public sealed interface Computable permits BoxedExprNum, BoxedText, BoxedNum, BoxedFormErr, BoxedCyclErr {
}
