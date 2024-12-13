public class CalculatorApp {
    public static void main(String[] args) {
        Calculator<Integer> integerCalculator = new Calculator<>();
        Calculator<Double> doubleCalculator = new Calculator<>();
        Calculator<Float> floatCalculator = new Calculator<>();
        Calculator<Long> longCalculator = new Calculator<>();
        Calculator<Short> shortCalculator = new Calculator<>();
        Calculator<Byte> byteCalculator = new Calculator<>();

        System.out.println(integerCalculator.calculate(1, 2, OperatorType.getOperatorType('+')));
        System.out.println(doubleCalculator.calculate(1.0, 2.0, OperatorType.getOperatorType('+')));
        System.out.println(floatCalculator.calculate(1F, 2F, OperatorType.getOperatorType('+')));
        System.out.println(longCalculator.calculate(1L, 2L, OperatorType.getOperatorType('+')));
        System.out.println(shortCalculator.calculate((short) 1, (short) 2, OperatorType.getOperatorType('+')));
        System.out.println(byteCalculator.calculate((byte) 1, (byte) 2, OperatorType.getOperatorType('+')));

        System.out.println(integerCalculator.getHighResultsThan(Integer.MIN_VALUE));
        System.out.println(doubleCalculator.getHighResultsThan(Double.MIN_VALUE));
        System.out.println(floatCalculator.getHighResultsThan(Float.MIN_VALUE));
        System.out.println(longCalculator.getHighResultsThan(Long.MIN_VALUE));
        System.out.println(shortCalculator.getHighResultsThan(Short.MIN_VALUE));
        System.out.println(byteCalculator.getHighResultsThan(Byte.MIN_VALUE));
    }
}
