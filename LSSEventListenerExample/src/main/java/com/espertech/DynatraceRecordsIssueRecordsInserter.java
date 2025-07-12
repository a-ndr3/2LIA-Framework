package com.espertech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DynatraceRecordsIssueRecordsInserter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final Random RANDOM = new Random();
    private static final int DEFAULT_MAX_INSERTIONS = 5;

    /**
     * Inserts records from the specified issue files into the original JSON file in random places, default is 5 insertions
     *
     * @return The path to the temporary file containing the modified records or the original file name if an error occurs
     */
    public static String seedErrors(String sourceFileName, List<String> issuesFileNames, Optional<Integer> maxInsertions) throws IOException {
        if (maxInsertions.isPresent() && maxInsertions.get() == 0)
            return sourceFileName;
        try {
            var mapper = new ObjectMapper();

            var originalFile = (new ClassPathResource(sourceFileName)).getFile();
            var originalJson = (ObjectNode) mapper.readTree(originalFile);

            List<File> jsonFilesFromIssues = new ArrayList<>();
            for (var issue : issuesFileNames) {
                var issueFile = (new ClassPathResource(issue)).getFile();
                if (issueFile.exists() && issueFile.isFile()) {
                    jsonFilesFromIssues.add(issueFile);
                }
            }

            List<List<ObjectNode>> recordGroupsToInsert = new ArrayList<>();
            for (File f : jsonFilesFromIssues) {
                var root = mapper.readTree(f);
                var recordsNode = root.get("records");
                if (recordsNode != null && recordsNode.isArray()) {
                    List<ObjectNode> group = new ArrayList<>();
                    for (var record : recordsNode) {
                        if (record.isObject()) {
                            group.add((ObjectNode) record);
                        }
                    }
                    if (!group.isEmpty()) {
                        recordGroupsToInsert.add(group);
                    }
                }
            }

            return insertRandomRecordLists(originalJson, recordGroupsToInsert, maxInsertions.orElse(DEFAULT_MAX_INSERTIONS));

        } catch (Exception e) {
            Main.logger.error("Error while inserting seeded issues into records: {}", e.getMessage());
            return sourceFileName;
        }
    }

    private static String insertRandomRecordLists(ObjectNode jsonRoot, List<List<ObjectNode>> recordGroupsToInsert, Integer maxInsertions) {
        var records = (ArrayNode) jsonRoot.get("records");

        for (List<ObjectNode> group : recordGroupsToInsert) {

            for (int insertion = 0; insertion < maxInsertions; insertion++) {
                int insertPos = RANDOM.nextInt(records.size() + 1);

                OffsetDateTime baseTime;
                if (insertPos > 0) {
                    var prevTimeStr = records.get(insertPos - 1).get("start_time").asText();
                    baseTime = OffsetDateTime.parse(prevTimeStr, FORMATTER);
                } else {
                    baseTime = OffsetDateTime.parse(records.get(0).get("start_time").asText(), FORMATTER).minusMinutes(1);
                }

                for (int i = 0; i < group.size(); i++) {
                    var recordCopy = group.get(i).deepCopy();
                    var newTime = baseTime.plusMinutes(i + 1);
                    recordCopy.put("start_time", newTime.format(FORMATTER));
                    recordCopy.put("span.source", "SEEDED_ISSUE_RECORD");
                    recordCopy.put("trace.id", recordCopy.get("trace.id").asText() + "_seeded_issue_record_" + insertion);
                    records.insert(insertPos + i, recordCopy);
                }
            }
        }

        return buildTemporaryFile(records);
    }

    private static String buildTemporaryFile(JsonNode records) {
        try {
            var mapper = new ObjectMapper();
            ObjectNode wrappedRoot;

            wrappedRoot = mapper.createObjectNode();
            wrappedRoot.set("records", records);

            var tempFile = File.createTempFile("recordsWithSeededIssues", ".json");
            mapper.writeValue(tempFile, wrappedRoot);
            return tempFile.getAbsolutePath();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
