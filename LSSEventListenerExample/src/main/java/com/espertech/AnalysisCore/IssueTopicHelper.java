package com.espertech.AnalysisCore;

import com.espertech.EventTypes.LSSEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public enum IssueTopicHelper {
    NETWORK,
    EXCEPTION,
    ENDPOINT,
    TIME,
    GENERIC;

    public static List<String> getProperties() {
        return List.of(NETWORK.name().toLowerCase(),
                EXCEPTION.name().toLowerCase(),
                ENDPOINT.name().toLowerCase(),
                TIME.name().toLowerCase(),
                GENERIC.name().toLowerCase());
    }

    public static IssueTopicHelper fromString(String name) {
        for (IssueTopicHelper topic : IssueTopicHelper.values()) {
            if (topic.name().equalsIgnoreCase(name)) {
                return topic;
            }
        }
        throw new IllegalArgumentException("No IssueTopicHelper found for name: " + name);
    }

    public static String getTopicForEvent(LSSEvent event, String statementName) {
       String issuePrefix;
       if (statementName.contains("network")) {
           issuePrefix = NETWORK.name().toLowerCase();
       } else if (statementName.contains("exception")) {
           issuePrefix = EXCEPTION.name().toLowerCase();
       } else if (statementName.contains("endpoint")) {
           issuePrefix = ENDPOINT.name().toLowerCase();
       } else if (statementName.contains("time")) {
           issuePrefix = TIME.name().toLowerCase();
       } else {
           issuePrefix = GENERIC.name().toLowerCase();
       }
       return issuePrefix + "-" + event.getClass().getSimpleName().toLowerCase() + "-" + statementName;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}


/*
 private final Properties issueMappings = new Properties();

    private static IssueTopicHelper instance = null;

    public static IssueTopicHelper getInstance() {
        if (instance == null) {
            instance = new IssueTopicHelper();
        }
        return instance;
    }

    private IssueTopicHelper() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("issue-mapping.properties")) {
            if (input != null) {
                issueMappings.load(input);
            } else {
                throw new IllegalStateException("issue-mapping.properties not found in resources.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load issue mappings", e);
        }
    }

    public List<String> getProperties() {
        return issueMappings.keySet().stream()
                .map(Object::toString)
                .toList();
    }

    public String extractIssueFromStatement(String name) {
        return issueMappings.keySet().stream()
                .map(Object::toString)
                .filter(name::contains)
                .map(issueMappings::getProperty).findAny().get();
    }

    public String getTopicForEvent(LSSEvent event, String statementName) {
        var issuePrefix = extractIssueFromStatement(statementName);
        return issuePrefix + "-" + event.getClass().getSimpleName().toLowerCase() + "-" + statementName;
    }
 */