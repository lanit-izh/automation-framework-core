package ru.lanit.at.extensions;


///**
// * define new annotation @ContainClass
// * forming short xpath using attribute
// */
//public class ContainsClassExtension implements MethodExtension {
//
//    @Override
//    public boolean test(Method method) {
//        return method
//                .isAnnotationPresent(ContainsClass.class);
//    }
//
//    @Override
//    public Object invoke(Object proxy, MethodInfo methodInfo, Configuration configuration) {
//        assert proxy instanceof SearchContext;
//        String classNmae = methodInfo.getMethod().getAnnotation(ContainsClass.class).value();
//        String xpath = format(".//*[contains(@class, '%s')]", classNmae);
//        SearchContext context = (SearchContext) proxy;
//        return new Atlas(configuration).create(context.findElement(By.xpath(xpath)), methodInfo.getMethod().getReturnType());
//    }
//
//}
