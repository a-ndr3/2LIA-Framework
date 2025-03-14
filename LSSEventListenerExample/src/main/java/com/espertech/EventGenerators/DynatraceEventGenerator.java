package com.espertech.EventGenerators;

import com.espertech.EventTypes.Types.DynatraceEvent;
import com.espertech.EventTypes.Types.DynatraceEventResponseField;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DynatraceEventGenerator {
    List<DynatraceEvent> events = new ArrayList<>();

    public DynatraceEventGenerator() {
    }

    public List<DynatraceEvent> readFile(String path) throws IOException {
        events.clear();
        generateEvents(path);
        return events;
    }

    public List<DynatraceEvent> getEvents() {
        return events;
    }

    private void generateEvents(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] fields = line.split(",");
            var event = new DynatraceEvent();

            event.timestamp = parseDate(fields[0]);
            event.accountId = parseInteger(fields[1]);
            event.bizflowName = parseString(fields[2]);
            event.bizflowEntity = parseString(fields[3]);
            event.host = parseString(fields[4]);
            event.processGroup = parseString(fields[5]);
            event.processGroupInstance = parseString(fields[6]);
            event.pipeline = parseString(fields[7]);
            event.source = parseString(fields[8]);
            event.category = parseString(fields[9]);
            event.eventId = parseString(fields[10]);
            event.kind = parseString(fields[11]);
            event.provider = parseString(fields[12]);
            event.type = parseString(fields[13]);
            event.spanId = parseString(fields[14]);
            event.traceId = parseString(fields[15]);
            event.traceSampled = parseBoolean(fields[16]);
            event.traceParent = parseString(fields[17]);
            event.array = parseBoolean(fields[18]);
            event.firstId = parseInteger(fields[19]);
            event.firstName = parseString(fields[20]);
            event.description = parseString(fields[21]);
            event.enabled = parseBoolean(fields[22]);
            event.flagId = parseString(fields[23]);
            event.isModifiable = parseBoolean(fields[24]);
            event.name = parseString(fields[25]);
            event.tag = parseString(fields[26]);
            event.accountActive = parseBoolean(fields[27]);
            event.address = parseString(fields[28]);
            event.creationDate = parseDate(fields[29]);
            event.email = parseString(fields[30]);
            event.lastName = parseString(fields[31]);
            event.origin = parseString(fields[32]);
            event.packageActivationDate = parseDate(fields[33]);
            event.packageId = parseString(fields[34]);
            event.username = parseString(fields[35]);
            event.response = DynatraceEventResponseField.parsePlatformData(fields[36]);
            event.amount = parseBigDecimal(fields[37]);
            event.balance = parseBigDecimal(fields[38]);
            event.cardNumber = parseString(fields[39]);
            event.cardType = parseString(fields[40]);
            event.cvv = parseInteger(fields[41]);
            event.firstPrice = parseBigDecimal(fields[42]);
            event.firstSupport = parseString(fields[43]);
            event.responseAfterFirstSupport = DynatraceEventResponseField.parsePackageSupport(fields[44]);
            event.maxYearlyFeeFilter = parseInteger(fields[45]);
            event.count = parseInteger(fields[46]);
            event.onlyLong = parseBoolean(fields[47]);
            event.firstAmount = parseInteger(fields[48]);
            event.firstCode = parseString(fields[49]);
            event.firstPriceClose = parseBigDecimal(fields[50]);
            event.firstProductId = parseInteger(fields[51]);
            event.records = parseInteger(fields[52]);
            event.firstClose = parseBigDecimal(fields[53]);
            event.firstInstrumentId = parseInteger(fields[54]);
            event.fullRequest = parseString(fields[55]);
            event.password = parseString(fields[56]);
            event.xml = parseBoolean(fields[57]);
            event.message = parseString(fields[58]);
            event.duration = parseLong(fields[59]);
            event.instrumentId = parseInteger(fields[60]);
            event.price = parseBigDecimal(fields[61]);
            event.hashedPassword = parseString(fields[62]);
            event.preset = parseBoolean(fields[63]);
            event.headers = parseString(fields[64]);
            event.fullResponse = parseString(fields[65]);
            event.statusCode = parseInteger(fields[66]);
            event.creditCardOrderId = parseString(fields[67]);
            event.details = parseString(fields[68]);
            event.resultsId = parseInteger(fields[69]);
            event.status = parseString(fields[70]);
            event.time = parseDate(fields[71]);
            event.productFilter = parseString(fields[72]);
            event.direction = parseString(fields[73]);
            event.happened = parseBoolean(fields[74]);
            event.requestHeaders = parseString(fields[75]);
            event.id = parseInteger(fields[76]);
        }
    }

    private static String parseString(String val) {
        return val == null || val.isEmpty() ? null : val;
    }

    private static Integer parseInteger(String val) {
        try { return val == null || val.isEmpty() ? null : Integer.parseInt(val); }
        catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal parseBigDecimal(String val) {
        try { return val == null || val.isEmpty() ? null : new BigDecimal(val); }
        catch (Exception e) { return null; }
    }

    private static boolean parseBoolean(String val) {
        return val != null && !val.isEmpty() && Boolean.parseBoolean(val);
    }

    private static Date parseDate(String val) {
        try { return val == null || val.isEmpty() ? null : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(val); }
        catch (Exception e) { return null; }
    }

    private static Long parseLong(String val) {
        try { return val == null || val.isEmpty() ? null : Long.parseLong(val); }
        catch (NumberFormatException e) { return null; }
    }
}
