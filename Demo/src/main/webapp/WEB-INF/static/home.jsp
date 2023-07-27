<%@ page import="org.demo.services.UserService" %>
<%@ page import="org.injection.InjectionConfig" %>
<%@ page import="org.demo.services.OrderService" %>
<%@ page import="org.injection.core.scopes.ScopeLifeCycle" %>
<%@ page import="org.injection.core.data.BeanInstance" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.lang.annotation.Annotation" %>
<%@ page import="org.web.annotations.scopes.SessionScope" %>
<%@ page import="org.demo.services.ProductService" %>
<%@ page import="org.injection.annotations.qualifiers.markers.FirstFound" %>
<%@ page import="org.web.annotations.scopes.RequestScope" %>
<%@ page import="org.web.WebProvider" %>
<%@ page import="org.web.Constants" %>
<%@ page import="org.injection.core.global.BeanContainer" %>
<%@ page import="org.web.WebConfig" %>
<%@ page import="org.web.WebProviderBuilder" %>

<%
    WebProvider webProvider = (WebProvider) request.getAttribute(Constants.WEB_PROVIDER);
    ProductService productService1 = webProvider.getBeanInstance(ProductService.class, SessionScope.class);
    ProductService productService2 = webProvider.getBeanInstance(ProductService.class, SessionScope.class);
    UserService userService = webProvider.getBeanInstance(UserService.class, SessionScope.class, FirstFound.class);
    OrderService orderService1 = webProvider.getBeanInstance(OrderService.class, RequestScope.class);
    OrderService orderService2 = webProvider.getBeanInstance(OrderService.class, RequestScope.class);
    WebProviderBuilder webProviderBuilder = (WebProviderBuilder)request.getAttribute(Constants.WEB_PROVIDER_BUILDER);
    InjectionConfig injectionConfig = webProviderBuilder.getInjectionConfig();
    BeanContainer beanContainer = injectionConfig.getBeanContainer();
    Set<BeanInstance> containerCache = beanContainer.getBeansWithFilter((beanInstance)->true);
%>

<html>
<body>

<form action="home.jsp" method="get">
    <input type="submit" name="command" value="Refresh!">
</form>

UserService obj: <%=userService%> <br/><br/>
OrderService obj_1: <%=orderService1%> <br/>
OrderService obj_2: <%=orderService2%> <br/><br/>
ProductService obj_1: <%=productService1%> <br/>
ProductService obj_2: <%=productService2%> <br/><br/>

Container cache size: <%=containerCache.size()%> <br/>
Scope cache size: <%=ScopeLifeCycle.getCache().size()%><br/><br/>

Current session: <%=request.getSession()%><br/>
Current Web provider session: <%=webProvider.getSession()%><br/><br/>

Current Request: <%=request%><br/>
Current Web provider request: <%=webProvider.getRequest()%><br/><br/>

<%
    String content="<h3>Beans tree:</h3>";
    content+="<ul>";
    Map<Class<? extends Annotation>, Set<Object>> scopeCache = ScopeLifeCycle.getCache();
  for (Class scopeType : scopeCache.keySet()) {
      content+= "<li>"+scopeType.getCanonicalName()+"</li>";
      content+= "<ul>";
      for(Object scopeInstance : scopeCache.get(scopeType)){
          content+= "<li>"+scopeInstance+"</li>";
          Set<BeanInstance> scopeBeans = beanContainer
                  .getBeansWithFilter((beanInstance)-> beanInstance.getScopeId().equals(scopeInstance));
          content+= "<ol>";
          for(BeanInstance beanInstance : scopeBeans)
              content+= "<li>"+beanInstance.getInstance()+"</li>";
          content+= "</ol>";
      }
      content+= "</ul>";
  }
    content+="</ul>";
%>

<%=content%>

</body>
</html>
