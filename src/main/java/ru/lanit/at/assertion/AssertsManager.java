package ru.lanit.at.assertion;

public class AssertsManager {
    private ExtendedAssert asserts;

    public AssertsManager() {
        this.asserts = new ExtendedAssert();
    }

    public ExtendedAssert criticalAssert() {
        asserts.setCritical();
        return asserts;
    }

    public ExtendedAssert softAssert(){
        return asserts;
    }

    public void flushAsserts(){
        this.asserts = new ExtendedAssert();
    }

}
