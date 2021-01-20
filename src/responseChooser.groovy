// Create request object
def request = new com.eviware.soapui.support.XmlHolder(mockRequest.requestContent)

// Get request and service types
def serviceType = context.mockService.getName()
def requestType = request.getDomNode("//*:Body/*").getLocalName()
def rulesTableName = serviceType + "_" + requestType
log.info("==========> SERVICE TYPE: $serviceType")
log.info("==========> REQUEST TYPE: $requestType")
log.info("==========> RULES TABLE NAME: $rulesTableName")

// Get rule from query_fields
def selectors = [:]
context.sql.query("select FIELD, SELECTOR from query_fields where SERVICE = $serviceType and REQUEST = $requestType") { resultSet ->
    while (resultSet.next()) {
        def selectorValue = request.getNodeValue(resultSet.getString('SELECTOR'))
        if (selectorValue != null) {
            selectors.put(resultSet.getString('FIELD'), selectorValue)
        } else selectors.put(resultSet.getString('FIELD'), null)
    }
}
log.info("==========> SELECTORS: $selectors")

// Build query for request to DB
def queryString = new StringBuilder().append("select response_xml from $rulesTableName where 1 = 1 ")
selectors.each {key, val ->
    if (val != null) {
        queryString.append("and $key = $val ")
    } else queryString.append("and $key is $val ")
}
log.info("==========> REQUEST TO DB QUERY: $queryString")

// Getting rules from DB
def row = context.sql.firstRow(queryString + ";")
if (row != null) {
    requestContext.response_xml = row.response_xml
}
