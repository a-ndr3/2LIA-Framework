class ApiEndpoints {
  static const String baseUrl = "http://localhost:8081/";
  static const String queries = "$baseUrl/queries/";
  static const String onDemand = "$queries/onDemandQuery";
  static const String checkExistingQueriesInDatabase =
      "$queries/checkExistingQueriesInDatabase";
  static const String changeQuery = "$queries/changeQuery";
  static const String deployQueryFromDB = "$queries/deployQueryFromDB";
  static const String pingDB = "$queries/pingDB";
  static const String grafana = "http://localhost:3000/login";
}
