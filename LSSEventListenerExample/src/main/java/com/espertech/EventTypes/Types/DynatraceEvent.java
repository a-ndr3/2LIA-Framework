package com.espertech.EventTypes.Types;

import com.espertech.EventTypes.LSSEvent;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.math.BigDecimal;
import java.util.Date;

public class DynatraceEvent implements LSSEvent {

    public Date timestamp;
    public Integer accountId;
    public String bizflowName;
    public String bizflowEntity;
    public String host;
    public String processGroup;
    public String processGroupInstance;
    public String pipeline;
    public String source;
    public String category;
    public String eventId;
    public String kind;
    public String provider;
    public String type;
    public String spanId;
    public String traceId;
    public boolean traceSampled;
    public String traceParent;
    public boolean array;
    public Integer firstId;
    public String firstName;
    public String description;
    public boolean enabled;
    public String flagId;
    public boolean isModifiable;
    public String name;
    public String tag;
    public boolean accountActive;
    public String address;
    public Date creationDate;
    public String email;
    public String lastName;
    public String origin;
    public Date packageActivationDate;
    public String packageId;
    public String username;
    public DynatraceEventResponseField.PlatformData response;
    public BigDecimal amount;
    public BigDecimal balance;
    public String cardNumber;
    public String cardType;
    public Integer cvv;
    public BigDecimal firstPrice;
    public String firstSupport;
    public DynatraceEventResponseField.PackageSupport responseAfterFirstSupport;
    public Integer maxYearlyFeeFilter;
    public Integer count;
    public boolean onlyLong;
    public Integer firstAmount;
    public String firstCode;
    public BigDecimal firstPriceClose;
    public Integer firstProductId;
    public Integer records;
    public BigDecimal firstClose;
    public Integer firstInstrumentId;
    public String fullRequest;
    public String password;
    public boolean xml;
    public String message;
    public Long duration;
    public Integer instrumentId;
    public BigDecimal price;
    public String hashedPassword;
    public boolean preset;
    public String headers;
    public String fullResponse;
    public Integer statusCode;
    public String creditCardOrderId;
    public String details;
    public Integer resultsId;
    public String status;
    public Date time;
    public String productFilter;
    public String direction;
    public boolean happened;
    public String requestHeaders;
    public Integer id;


    @Override
    public String getClassType() {
        return DynatraceEvent.class.getName();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getBizflowName() {
        return bizflowName;
    }

    public void setBizflowName(String bizflowName) {
        this.bizflowName = bizflowName;
    }

    public String getBizflowEntity() {
        return bizflowEntity;
    }

    public void setBizflowEntity(String bizflowEntity) {
        this.bizflowEntity = bizflowEntity;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProcessGroup() {
        return processGroup;
    }

    public void setProcessGroup(String processGroup) {
        this.processGroup = processGroup;
    }

    public String getProcessGroupInstance() {
        return processGroupInstance;
    }

    public void setProcessGroupInstance(String processGroupInstance) {
        this.processGroupInstance = processGroupInstance;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public boolean isTraceSampled() {
        return traceSampled;
    }

    public void setTraceSampled(boolean traceSampled) {
        this.traceSampled = traceSampled;
    }

    public String getTraceParent() {
        return traceParent;
    }

    public void setTraceParent(String traceParent) {
        this.traceParent = traceParent;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public Integer getFirstId() {
        return firstId;
    }

    public void setFirstId(Integer firstId) {
        this.firstId = firstId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFlagId() {
        return flagId;
    }

    public void setFlagId(String flagId) {
        this.flagId = flagId;
    }

    public boolean isModifiable() {
        return isModifiable;
    }

    public void setModifiable(boolean modifiable) {
        isModifiable = modifiable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isAccountActive() {
        return accountActive;
    }

    public void setAccountActive(boolean accountActive) {
        this.accountActive = accountActive;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Date getPackageActivationDate() {
        return packageActivationDate;
    }

    public void setPackageActivationDate(Date packageActivationDate) {
        this.packageActivationDate = packageActivationDate;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DynatraceEventResponseField.PlatformData getResponse() {
        return response;
    }

    public void setResponse(DynatraceEventResponseField.PlatformData response) {
        this.response = response;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Integer getCvv() {
        return cvv;
    }

    public void setCvv(Integer cvv) {
        this.cvv = cvv;
    }

    public BigDecimal getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(BigDecimal firstPrice) {
        this.firstPrice = firstPrice;
    }

    public String getFirstSupport() {
        return firstSupport;
    }

    public void setFirstSupport(String firstSupport) {
        this.firstSupport = firstSupport;
    }

    public DynatraceEventResponseField.PackageSupport getResponseAfterFirstSupport() {
        return responseAfterFirstSupport;
    }

    public void setResponseAfterFirstSupport(DynatraceEventResponseField.PackageSupport responseAfterFirstSupport) {
        this.responseAfterFirstSupport = responseAfterFirstSupport;
    }

    public Integer getMaxYearlyFeeFilter() {
        return maxYearlyFeeFilter;
    }

    public void setMaxYearlyFeeFilter(Integer maxYearlyFeeFilter) {
        this.maxYearlyFeeFilter = maxYearlyFeeFilter;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public boolean isOnlyLong() {
        return onlyLong;
    }

    public void setOnlyLong(boolean onlyLong) {
        this.onlyLong = onlyLong;
    }

    public Integer getFirstAmount() {
        return firstAmount;
    }

    public void setFirstAmount(Integer firstAmount) {
        this.firstAmount = firstAmount;
    }

    public String getFirstCode() {
        return firstCode;
    }

    public void setFirstCode(String firstCode) {
        this.firstCode = firstCode;
    }

    public BigDecimal getFirstPriceClose() {
        return firstPriceClose;
    }

    public void setFirstPriceClose(BigDecimal firstPriceClose) {
        this.firstPriceClose = firstPriceClose;
    }

    public Integer getFirstProductId() {
        return firstProductId;
    }

    public void setFirstProductId(Integer firstProductId) {
        this.firstProductId = firstProductId;
    }

    public Integer getRecords() {
        return records;
    }

    public void setRecords(Integer records) {
        this.records = records;
    }

    public BigDecimal getFirstClose() {
        return firstClose;
    }

    public void setFirstClose(BigDecimal firstClose) {
        this.firstClose = firstClose;
    }

    public Integer getFirstInstrumentId() {
        return firstInstrumentId;
    }

    public void setFirstInstrumentId(Integer firstInstrumentId) {
        this.firstInstrumentId = firstInstrumentId;
    }

    public String getFullRequest() {
        return fullRequest;
    }

    public void setFullRequest(String fullRequest) {
        this.fullRequest = fullRequest;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Integer instrumentId) {
        this.instrumentId = instrumentId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public boolean isPreset() {
        return preset;
    }

    public void setPreset(boolean preset) {
        this.preset = preset;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getFullResponse() {
        return fullResponse;
    }

    public void setFullResponse(String fullResponse) {
        this.fullResponse = fullResponse;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getCreditCardOrderId() {
        return creditCardOrderId;
    }

    public void setCreditCardOrderId(String creditCardOrderId) {
        this.creditCardOrderId = creditCardOrderId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getResultsId() {
        return resultsId;
    }

    public void setResultsId(Integer resultsId) {
        this.resultsId = resultsId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getProductFilter() {
        return productFilter;
    }

    public void setProductFilter(String productFilter) {
        this.productFilter = productFilter;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isHappened() {
        return happened;
    }

    public void setHappened(boolean happened) {
        this.happened = happened;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
