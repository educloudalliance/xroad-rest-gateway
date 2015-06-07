<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
    </head>
    <body>
        <h3>REST Gateway</h3>
        <p>REST Gateway on <a href="https://github.com/educloudalliance/xroad-rest-gateway" target="new">GitHub</a>.</p>
        <p><b>Consumer</b></p>
        <p>Consumer endpoint: <a href="${pageContext.request.requestURL}Consumer">${pageContext.request.requestURL}Consumer</a></p>
        <div>
            Properties:
            <ul>
                <li>WEB-INF/classes/consumer-gateway.properties</li>
                <li>WEB-INF/classes/consumers.properties</li>
            </ul>
        </div>
        <p><b>Provider</b></p>
        <p>Provider endpoint: <a href="${pageContext.request.requestURL}Provider">${pageContext.request.requestURL}Provider</a></p>
        <p>Provider WSDL: <a href="${pageContext.request.requestURL}Provider?wsdl">${pageContext.request.requestURL}Provider?wsdl</a></p>
        <p>WSDL path: <i>WEB-INF/classes/provider-gateway.wsdl</i></p>
        <div>
            Properties:
            <ul>
                <li>WEB-INF/classes/provider-gateway.properties</li>
                <li>WEB-INF/classes/providers.properties</li>
            </ul>
        </div>       
    </body>
</html>