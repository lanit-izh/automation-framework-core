package ru.lanit.at.assertion;

public class AssertsManager {
    private ExtendedAssert asserts;

    public AssertsManager() {
        this.asserts = new ExtendedAssert();
    }

    /**
     * Method to get {@link ExtendedAssert} previously marked as critical. So such assert will fail all test.
     *
     * @return Critical {@link ExtendedAssert}.
     */
    public ExtendedAssert criticalAssert() {
        asserts.setCritical();
        return asserts;
    }

    /**
     * Method to get soft {@link ExtendedAssert} that collects assertions.
     *
     * @return Soft {@link ExtendedAssert}.
     */
    public ExtendedAssert softAssert() {
        return asserts;
    }

    /**
     * Recreates instance of {@link ExtendedAssert}.
     */
    public void flushAsserts() {
        this.asserts = new ExtendedAssert();
    }

}
