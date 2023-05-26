package org.demo;

import com.sun.net.httpserver.HttpHandler;
import org.aspect.agent.Loader;
import org.aspect.proxy.Wrapper;
import org.demo.Server.Server;
import org.demo.dao.UserDAO;
import org.demo.services.ProductService;
import org.demo.services.UserService;
import org.demo.services.impls.UserServiceImpl;
import org.injection.DefaultProvider;
import org.injection.InjectionConfig;
import org.injection.annotations.AlternativeConfig;
import org.injection.annotations.BeanScanPackages;
import org.injection.annotations.Component;
import org.injection.core.providers.BeanProvider;
import org.injection.core.data.AlternativeInstance;
import org.injection.core.data.ScopeInstance;
import org.injection.core.providers.BeanResolverImpl;
import org.injection.others.DebuggerInstance;
import org.injection.others.TestBean;
import org.tools.GlobalTools;
import org.tools.Log;
import org.tools.agent.AgentLoader;
import org.tools.annotations.AnnotationTools;
import org.tools.annotations.AnnotationTree;
import org.web.WebConfig;
import org.web.annotations.WebScanPackages;
import org.web.filters.basic.GzipFilter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Scope;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

@WebScanPackages(packages = {"org.demo.controllers"})
@BeanScanPackages(
        packages = {"org.demo"},
        excludes = {".*(Server).*",".*(dao).*"}
        //excludes = {".*(org.demo).*"} // exclude all
)
public class Launcher {
    private static final Log logger = Log.getInstance(Launcher.class);
    @Inject
    public String field;

    public static void main(String[] args) throws Exception {
        //testCustomQualifierManager();
        //testTools();
        //testAnnotationTools();
        testJDKProxyAspect();
        testCglibProxyAspect();
        //testAgentAspect();
        //testDI();
        //ClassFinder.addToClassPath(Collections.singleton("path."+System.currentTimeMillis()+".jar"));
        //System.out.println(WebConfig.getControllerProcessor().getRouteHandlers());
        //testWeb();
        //testBeanResolver();
        //testProxy();
        //testDebugger();
        //testBuildAgent();
        //testSearchAgent();
        //testAnnotationTree();

    }

    public static void testBeanResolver(){
        BeanResolverImpl beanResolver = new BeanResolverImpl(
                InjectionConfig.getDefaultInstance().getBeanScanManager(),
                InjectionConfig.getDefaultInstance().getBeanQualifierManager()
        );
        ProductService service = beanResolver
                .getBeanInstance(ProductService.class, null);
        System.out.println(service);
    }
    @AlternativeConfig
    public Set<AlternativeInstance> getAlternatives(){
        Set<AlternativeInstance> alternatives = new HashSet<>();
        //alternatives.add(new AlternativeInstance(DBService.class, DBServiceImpl_2.class));
        return alternatives;
    }

    public static void testAnnotationTree(){
        logger.debug("RootAnnotations: [" + AnnotationTree.getRootAnnotations().size() +"]"+ AnnotationTree.getRootAnnotations());
        logger.debug("LeafAnnotations:  [" + AnnotationTree.getLeafAnnotations().size() +"]"  + AnnotationTree.getLeafAnnotations());
        logger.debug("OrphanAnnotations:  [" + AnnotationTree.getOrphanAnnotations().size() +"]"  + AnnotationTree.getOrphanAnnotations());
        logger.debug("AllAvailableAnnotations:  [" + AnnotationTree.getAllAvailableAnnotations().size() +"]"  + AnnotationTree.getAllAvailableAnnotations());
        logger.debug("PathOfAnnotations:  [" + AnnotationTree.getPathOfAnnotations(TestBean.class, Component.class).size() +"]" + AnnotationTree.getPathOfAnnotations(TestBean.class, Component.class));

    }

    public static void testBuildAgent() throws Exception {
        Map<String, Object> options = AgentLoader.getDefaultOptions();
        options.put("type", "lifecycle");
        options.put("cnfr", "(org\\.demo\\.).*"); // class name filter regex
        AgentLoader.buildAndAttachAgent(
                Launcher.class.getCanonicalName(),
                "org.agent",
                "org.agent.AgentMain",
                options
                );
    }

    public static void testSearchAgent() throws Exception {
        Map<String, Object> options = AgentLoader.getDefaultOptions();
        options.put("type", "lifecycle");
        options.put("cnfr", "(org\\.demo\\.).*"); // class name filter regex
        AgentLoader.searchAndAttachAgent(
                Launcher.class.getCanonicalName(),
                "org.agent.AgentMain",
                options
        );
    }

    public static void testDebugger() throws IOException {
        DebuggerInstance debuggerInstance = new DebuggerInstance(Launcher.class, new int[]{54, 55});
        debuggerInstance.start(System.out);
    }
    public static void testProxy(){
        //ProxyHandler<UserDAO> proxyHandler =  ScopeManagerTest.createScope(UserInterface.class);
        //logger.debug(proxyHandler);
        UserDAO userDAO = new UserDAO();
        userDAO.setUsername("TOTO");
    }

    public static void testTools(){
        logger.debug(GlobalTools.matchAll("toti", new String[]{".*(to).*",".*(ti).*"}));
    }

    public static void testAnnotationTools() throws NoSuchFieldException {
        Field fieldRef = Launcher.class.getField("field");
        boolean exists = AnnotationTools.isAnnotationPresent(fieldRef, Scope.class);
        logger.debug(fieldRef.getName()+"  "+ exists);
    }

    public static void testDI() throws Exception {
        // dependency injection
        BeanProvider defaultBeanProvider = DefaultProvider.init();
        UserService userService = defaultBeanProvider.getBeanInstance(UserService.class);
        UserDAO userDAO = new UserDAO();
        userDAO.setUsername("TOTO");
        userDAO = userService.register(userDAO);
        logger.debug("userService register result: "+userDAO); // call method from first bean
        logger.debug("userService login result: "+userService.login(userDAO.getId())); // call method from first bean
        UserService userServiceWithBeanProvider = defaultBeanProvider
                .getBeanInstance(UserService.class);
        logger.debug(userServiceWithBeanProvider.login(userDAO.getId()));
    }

    public static void testAgentAspect(){
        Loader.init();
    }

    public static void testJDKProxyAspect(){
        BeanProvider defaultBeanProvider = DefaultProvider.init();
        UserService userService = defaultBeanProvider.getBeanInstance(UserService.class);
        UserService wrappedUserService = (UserService) Wrapper.wrap(userService);
        logger.debug("testProxyAspect: "+wrappedUserService.hashCode());
        logger.debug("testProxyAspect: "+wrappedUserService.login("toto"));
    }

    public static void testCglibProxyAspect(){
        class Toto{
            public String titi(){
                return "titi";
            }
        }
        Toto toto = new Toto();
        Toto wrappedToto = (Toto)Wrapper.wrap(toto);
        logger.debug("testCglibProxyAspect: "+wrappedToto.titi());
    }

    public static void testWeb() throws Exception {
        Map<String, HttpHandler> handlersMap = WebConfig
                .getControllerProcessor()
                .getHttpHandlers();
        handlersMap.put("/echo", httpExchange -> {
            String requestMethod = httpExchange.getRequestMethod();
            String requestPath = httpExchange.getRequestURI().getPath();
            logger.debug(String.format(
                    "New Request received, Method: %s, Path: %s\n",
                    requestMethod, requestPath
            ));
            // return response
            String result = "Hello, it's working..";
            httpExchange.sendResponseHeaders(200,result.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(result.getBytes());
            os.close();
        });
        Server.init(9090, handlersMap);
        Server.setFilters(Arrays.asList(new GzipFilter()));
        Server.start();
    }

    public static void testCustomQualifierManager(){
        /*BeanQualifierManager customBeanQualifierManager = new BeanQualifierManager() {
            final BeanQualifierManager defaultBeanQualifierManager = DefaultContext.getBeanQualifierManager();

            @Override
            public QualifierPredicate addQualifier(Class<? extends Annotation> qualifierAnnotationClass, QualifierPredicate qualifierPredicate) {
                return defaultBeanQualifierManager.addQualifier(qualifierAnnotationClass, qualifierPredicate);
            }

            @Override
            public QualifierPredicate removeQualifier(Class<? extends Annotation> qualifierAnnotationClass) {
                return defaultBeanQualifierManager.removeQualifier(qualifierAnnotationClass);
            }

            @Override
            public Map<Class<? extends Annotation>, QualifierPredicate> getAvailableQualifiers() {
                return defaultBeanQualifierManager.getAvailableQualifiers();
            }

            @Override
            public Set<Class> filterImplementations(Set<Class> beanImplementations, Set<Annotation> qualifiers) {
                if(qualifiers==null)
                    qualifiers = new HashSet<>();
                qualifiers.add(new Annotation() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ElseFirstFound.class;
                    }
                });
                return defaultBeanQualifierManager.filterImplementations(beanImplementations, qualifiers);
            }

            @Override
            public Set<Method> filterFactories(Set<Method> beanFactories, Set<Annotation> qualifiers) {
                return defaultBeanQualifierManager.filterFactories(beanFactories, qualifiers);
            }

            @Override
            public boolean match(Object beanSource, Set<Annotation> qualifiers, boolean withAndEvaluation) {
                return defaultBeanQualifierManager.match(beanSource, qualifiers, withAndEvaluation);
            }

            @Override
            public boolean isValidQualifierAnnotation(Class<? extends Annotation> annotationClass) {
                return defaultBeanQualifierManager.isValidQualifierAnnotation(annotationClass);
            }
        };
        DefaultContext.setBeanQualifierManager(customBeanQualifierManager);
        */Set<Annotation> qualifiers = Collections.singleton(new Named(){

            @Override
            public Class<? extends Annotation> annotationType() {
                return Named.class;
            }

            @Override
            public String value() {
                return "TOTO";
            }
        });
        Set<ScopeInstance> scopeInstances = new HashSet<>(); //Collections.singleton(new ScopeInstance(String.class, "testScope"));
        Object bean = DefaultProvider
                .init()
                .getBeanInstance(UserService.class, qualifiers, scopeInstances, String.class);
        System.out.println("Bean: "+bean);
    }
}


